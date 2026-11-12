package com.rayven0421.ashnotes.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.domain.model.Block
import com.rayven0421.ashnotes.domain.model.Document
import com.rayven0421.ashnotes.domain.model.FocusRequest
import com.rayven0421.ashnotes.domain.model.Note
import com.rayven0421.ashnotes.domain.model.TextBlock
import com.rayven0421.ashnotes.domain.model.TextBlockOperations
import com.rayven0421.ashnotes.domain.repository.NoteRepository
import com.rayven0421.ashnotes.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.reflect.KClass
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val noteId: String = savedStateHandle.toRoute<Screen.Editor>().noteId

    private val _uiState = MutableStateFlow<UiState<EditorState>>(UiState.Loading)
    val uiState: StateFlow<UiState<EditorState>> = _uiState.asStateFlow()

    private var currentNote: Note? = null
    private var lastSavedTitle: String = ""
    private var lastSavedContentJson: String = ""

    private val historyManager = EditorHistoryManager(maxHistory = 50)
    private var currentFocusedBlockId: String? = null
    private var currentCursorIndex: Int = 0

    private var typingSnapshotJob: Job? = null
    private var lastTextSnapshotEntry: HistoryEntry? = null

    private val autoSaveTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    init {
        loadNote()
        observeAutoSaveTrigger()
    }

    private fun loadNote() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val note = noteRepository.getNoteById(noteId)
            if (note == null) {
                _uiState.value = UiState.Error("Note not found")
                return@launch
            }

            currentNote = note
            lastSavedTitle = note.title

            val doc = if (note.contentJson.isNotBlank()) {
                try {
                    val parsed = json.decodeFromString(Document.serializer(), note.contentJson)
                    if (parsed.blocks.isEmpty()) createDefaultDocument() else parsed
                } catch (_: Exception) {
                    createDefaultDocument()
                }
            } else {
                createDefaultDocument()
            }

            lastSavedContentJson = json.encodeToString(Document.serializer(), doc)
            historyManager.clear()

            _uiState.value = UiState.Success(
                EditorState(
                    noteId = noteId,
                    title = note.title,
                    document = doc,
                    saveStatus = SaveStatus.Saved,
                    canUndo = historyManager.canUndo,
                    canRedo = historyManager.canRedo
                )
            )
        }
    }

    private fun createDefaultDocument(): Document {
        val now = System.currentTimeMillis()
        val defaultBlock = Block.Paragraph(
            id = UUID.randomUUID().toString(),
            position = 0,
            createdAt = now,
            updatedAt = now,
            text = ""
        )
        return Document(version = 1, blocks = listOf(defaultBlock))
    }

    private fun observeAutoSaveTrigger() {
        viewModelScope.launch {
            autoSaveTrigger
                .debounce(700L)
                .collect {
                    performSave()
                }
        }
    }

    fun onBlockFocused(blockId: String, cursorIndex: Int) {
        currentFocusedBlockId = blockId
        currentCursorIndex = cursorIndex
    }

    private fun pushUndoSnapshot(
        doc: Document,
        title: String,
        focusBlockId: String? = currentFocusedBlockId,
        focusCursor: Int = currentCursorIndex
    ) {
        val entry = HistoryEntry(
            document = doc,
            title = title,
            focusedBlockId = focusBlockId,
            cursorIndex = focusCursor
        )
        historyManager.pushSnapshot(entry)
        updateUndoRedoFlags()
    }

    private fun scheduleTypingSnapshot(doc: Document, title: String) {
        if (lastTextSnapshotEntry == null) {
            lastTextSnapshotEntry = HistoryEntry(
                document = doc,
                title = title,
                focusedBlockId = currentFocusedBlockId,
                cursorIndex = currentCursorIndex
            )
        }
        typingSnapshotJob?.cancel()
        typingSnapshotJob = viewModelScope.launch {
            delay(800L)
            lastTextSnapshotEntry?.let { snapshot ->
                historyManager.pushSnapshot(snapshot)
                updateUndoRedoFlags()
            }
            lastTextSnapshotEntry = null
        }
    }

    private fun cancelTypingSnapshot() {
        typingSnapshotJob?.cancel()
        lastTextSnapshotEntry = null
    }

    private fun updateUndoRedoFlags() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(
            currentState.copy(
                canUndo = historyManager.canUndo,
                canRedo = historyManager.canRedo
            )
        )
    }

    fun undo() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        if (!historyManager.canUndo) return

        cancelTypingSnapshot()
        val currentEntry = HistoryEntry(
            document = currentState.document,
            title = currentState.title,
            focusedBlockId = currentFocusedBlockId,
            cursorIndex = currentCursorIndex
        )
        val previousEntry = historyManager.undo(currentEntry) ?: return

        currentFocusedBlockId = previousEntry.focusedBlockId
        currentCursorIndex = previousEntry.cursorIndex

        val focusRequest = previousEntry.focusedBlockId?.let {
            FocusRequest(blockId = it, cursor = previousEntry.cursorIndex)
        }

        val newState = currentState.copy(
            document = previousEntry.document,
            title = previousEntry.title,
            saveStatus = SaveStatus.Saving,
            canUndo = historyManager.canUndo,
            canRedo = historyManager.canRedo,
            focusRequest = focusRequest
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun redo() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        if (!historyManager.canRedo) return

        cancelTypingSnapshot()
        val currentEntry = HistoryEntry(
            document = currentState.document,
            title = currentState.title,
            focusedBlockId = currentFocusedBlockId,
            cursorIndex = currentCursorIndex
        )
        val nextEntry = historyManager.redo(currentEntry) ?: return

        currentFocusedBlockId = nextEntry.focusedBlockId
        currentCursorIndex = nextEntry.cursorIndex

        val focusRequest = nextEntry.focusedBlockId?.let {
            FocusRequest(blockId = it, cursor = nextEntry.cursorIndex)
        }

        val newState = currentState.copy(
            document = nextEntry.document,
            title = nextEntry.title,
            saveStatus = SaveStatus.Saving,
            canUndo = historyManager.canUndo,
            canRedo = historyManager.canRedo,
            focusRequest = focusRequest
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun updateTitle(newTitle: String) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        if (currentState.title == newTitle) return

        scheduleTypingSnapshot(currentState.document, currentState.title)

        val newState = currentState.copy(
            title = newTitle,
            saveStatus = SaveStatus.Saving
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun updateParagraphText(blockId: String, newText: String) = updateTextBlockText(blockId, newText)
    fun updateHeadingText(blockId: String, newText: String) = updateTextBlockText(blockId, newText)
    fun updateChecklistText(blockId: String, newText: String) = updateTextBlockText(blockId, newText)

    fun updateTextBlockText(blockId: String, newText: String) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        scheduleTypingSnapshot(currentState.document, currentState.title)
        updateBlock(blockId) { block ->
            if (block is TextBlock) {
                block.withText(newText, System.currentTimeMillis())
            } else block
        }
    }

    fun toggleChecklistChecked(blockId: String) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        cancelTypingSnapshot()
        pushUndoSnapshot(currentState.document, currentState.title)
        val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState

        val updatedBlocks = latestState.document.blocks.map { block ->
            if (block.id == blockId && block is Block.Checklist) {
                block.copy(isChecked = !block.isChecked, updatedAt = System.currentTimeMillis())
            } else block
        }
        val updatedDocument = latestState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(updatedBlocks))
        val newState = latestState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    private fun updateBlock(blockId: String, transform: (Block) -> Block) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        val updatedBlocks = currentState.document.blocks.map { block ->
            if (block.id == blockId) transform(block) else block
        }
        val updatedDocument = currentState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(updatedBlocks))
        val newState = currentState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun flushSave() {
        viewModelScope.launch {
            performSave()
        }
    }

    fun appendParagraphIfNeeded() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        val blocks = currentState.document.blocks
        val lastBlock = blocks.lastOrNull()

        if (lastBlock is Block.Paragraph && lastBlock.text.isEmpty()) return

        cancelTypingSnapshot()
        pushUndoSnapshot(currentState.document, currentState.title)
        val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState

        val now = System.currentTimeMillis()
        val newParagraph = Block.Paragraph(
            id = UUID.randomUUID().toString(),
            position = latestState.document.blocks.size,
            createdAt = now,
            updatedAt = now,
            text = ""
        )
        val updatedDocument = latestState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(latestState.document.blocks + newParagraph))
        val newState = latestState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving,
            focusRequest = FocusRequest(blockId = newParagraph.id, cursor = 0)
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun splitParagraphBlock(blockId: String, cursorIndex: Int) = splitTextBlock(blockId, cursorIndex)
    fun splitHeadingBlock(blockId: String, cursorIndex: Int) = splitTextBlock(blockId, cursorIndex)
    fun splitChecklistBlock(blockId: String, cursorIndex: Int) = splitTextBlock(blockId, cursorIndex)

    fun splitTextBlock(blockId: String, cursorIndex: Int) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return

        val result = TextBlockOperations.split(
            currentState.document.blocks,
            blockId,
            cursorIndex
        )
        if (result.focusBlockId == null) return

        val updatedDocument = currentState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(result.blocks))
        if (updatedDocument == currentState.document) return

        cancelTypingSnapshot()
        pushUndoSnapshot(currentState.document, currentState.title)
        val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState

        val newState = latestState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving,
            focusRequest = FocusRequest(blockId = result.focusBlockId, cursor = result.focusCursorIndex)
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun pasteTextIntoParagraph(blockId: String, pastedText: String, cursorIndex: Int) = pasteTextIntoBlock(blockId, pastedText, cursorIndex)
    fun pasteTextIntoHeading(blockId: String, pastedText: String, cursorIndex: Int) = pasteTextIntoBlock(blockId, pastedText, cursorIndex)
    fun pasteTextIntoChecklist(blockId: String, pastedText: String, cursorIndex: Int) = pasteTextIntoBlock(blockId, pastedText, cursorIndex)

    fun pasteTextIntoBlock(blockId: String, pastedText: String, cursorIndex: Int) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return

        val result = TextBlockOperations.paste(
            currentState.document.blocks,
            blockId,
            pastedText,
            cursorIndex
        )
        if (result.focusBlockId == null) return

        val updatedDocument = currentState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(result.blocks))
        if (updatedDocument == currentState.document) return

        cancelTypingSnapshot()
        pushUndoSnapshot(currentState.document, currentState.title)
        val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState

        val newState = latestState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving,
            focusRequest = FocusRequest(blockId = result.focusBlockId, cursor = result.focusCursorIndex)
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun handleBackspaceInParagraph(blockId: String, cursorIndex: Int) = handleBackspaceInBlock(blockId, cursorIndex)
    fun handleBackspaceInChecklist(blockId: String, cursorIndex: Int) = handleBackspaceInBlock(blockId, cursorIndex)

    fun handleBackspaceInBlock(blockId: String, cursorIndex: Int) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return

        val result = TextBlockOperations.mergeOnBackspace(
            currentState.document.blocks,
            blockId,
            cursorIndex
        )
        if (result.focusBlockId == null) return

        val updatedDocument = currentState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(result.blocks))
        if (updatedDocument == currentState.document) return

        cancelTypingSnapshot()
        pushUndoSnapshot(currentState.document, currentState.title, focusBlockId = blockId, focusCursor = cursorIndex)
        val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState

        val newState = latestState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving,
            focusRequest = FocusRequest(blockId = result.focusBlockId, cursor = result.focusCursorIndex)
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun toggleChecklistForBlock(blockId: String) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        val targetBlock = currentState.document.blocks.find { it.id == blockId }
        if (targetBlock !is TextBlock) return

        val (updatedBlocks, focusRequest) = TextBlockOperations.toggleChecklistBlock(
            currentState.document.blocks,
            blockId,
            currentCursorIndex
        )

        val updatedDocument = currentState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(updatedBlocks))
        if (updatedDocument == currentState.document) return

        cancelTypingSnapshot()
        pushUndoSnapshot(currentState.document, currentState.title)
        val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState

        val newState = latestState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving,
            focusRequest = focusRequest
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun insertBlockBelow(type: KClass<out TextBlock>) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return

        val targetBlockId = currentFocusedBlockId ?: currentState.document.blocks.lastOrNull()?.id ?: ""
        val result = TextBlockOperations.insertBlockBelow(
            currentState.document.blocks,
            targetBlockId,
            type
        )
        if (result.focusBlockId == null) return

        val updatedDocument = currentState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(result.blocks))
        if (updatedDocument == currentState.document) return

        cancelTypingSnapshot()
        pushUndoSnapshot(currentState.document, currentState.title)
        val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState

        val newState = latestState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving,
            focusRequest = FocusRequest(blockId = result.focusBlockId, cursor = result.focusCursorIndex)
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun insertDivider() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return

        val targetBlockId = currentFocusedBlockId ?: currentState.document.blocks.lastOrNull()?.id ?: ""
        val result = TextBlockOperations.insertDivider(
            currentState.document.blocks,
            targetBlockId
        )
        if (result.focusBlockId == null) return

        val updatedDocument = currentState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(result.blocks))
        if (updatedDocument == currentState.document) return

        cancelTypingSnapshot()
        pushUndoSnapshot(currentState.document, currentState.title)
        val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState

        val newState = latestState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving,
            focusRequest = FocusRequest(blockId = result.focusBlockId, cursor = result.focusCursorIndex)
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun deleteBlock(blockId: String) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return

        val result = TextBlockOperations.deleteBlock(
            currentState.document.blocks,
            blockId
        )
        val updatedDocument = currentState.document.copy(blocks = TextBlockOperations.ensureNonEmptyBlocks(result.blocks))
        if (updatedDocument == currentState.document) return

        cancelTypingSnapshot()
        pushUndoSnapshot(currentState.document, currentState.title, focusBlockId = blockId, focusCursor = 0)
        val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState

        val focusRequest = result.focusBlockId?.let { FocusRequest(blockId = it, cursor = result.focusCursorIndex) }
        val newState = latestState.copy(
            document = updatedDocument,
            saveStatus = SaveStatus.Saving,
            focusRequest = focusRequest
        )
        _uiState.value = UiState.Success(newState)
        autoSaveTrigger.tryEmit(Unit)
    }

    fun onTapBelowBlocks() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        val blocks = currentState.document.blocks
        val lastBlock = blocks.lastOrNull()

        if (lastBlock is Block.Paragraph) {
            _uiState.value = UiState.Success(
                currentState.copy(
                    focusRequest = FocusRequest(blockId = lastBlock.id, cursor = lastBlock.text.length)
                )
            )
        } else {
            appendParagraphIfNeeded()
        }
    }

    fun clearFocusRequest(token: Long) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        if (currentState.focusRequest?.token == token) {
            _uiState.value = UiState.Success(currentState.copy(focusRequest = null))
        }
    }

    private suspend fun performSave() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        val note = currentNote ?: return

        val currentDocumentJson = json.encodeToString(Document.serializer(), currentState.document)

        if (currentState.title == lastSavedTitle && currentDocumentJson == lastSavedContentJson) {
            val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState
            _uiState.value = UiState.Success(latestState.copy(saveStatus = SaveStatus.Saved))
            return
        }

        try {
            val now = System.currentTimeMillis()
            val updatedNote = note.copy(
                title = currentState.title,
                contentJson = currentDocumentJson,
                documentVersion = currentState.document.version,
                updatedAt = now
            )
            noteRepository.updateNote(updatedNote)
            currentNote = updatedNote
            lastSavedTitle = currentState.title
            lastSavedContentJson = currentDocumentJson

            val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState
            val latestDocJson = json.encodeToString(Document.serializer(), latestState.document)

            if (latestState.title != currentState.title || latestDocJson != currentDocumentJson) {
                _uiState.value = UiState.Success(
                    latestState.copy(
                        saveStatus = SaveStatus.Saving,
                        errorMessage = null
                    )
                )
                autoSaveTrigger.tryEmit(Unit)
            } else {
                _uiState.value = UiState.Success(
                    latestState.copy(
                        saveStatus = SaveStatus.Saved,
                        errorMessage = null
                    )
                )
            }
        } catch (e: Exception) {
            val latestState = (_uiState.value as? UiState.Success)?.data ?: currentState
            _uiState.value = UiState.Success(
                latestState.copy(
                    saveStatus = SaveStatus.Error,
                    errorMessage = e.localizedMessage ?: "Failed to save note"
                )
            )
        }
    }
}
