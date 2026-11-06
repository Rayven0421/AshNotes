package com.rayven0421.ashnotes.feature.editor

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.Block
import com.rayven0421.ashnotes.domain.model.ChecklistOperations
import com.rayven0421.ashnotes.domain.model.Document
import com.rayven0421.ashnotes.domain.model.FocusRequest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun newNote_tapChecklist_exactlyOneChecklistTextNodeAndFocused() {
        var document by mutableStateOf(
            Document(
                version = 1,
                blocks = listOf(
                    Block.Paragraph(
                        id = "p1",
                        position = 0,
                        createdAt = 1000L,
                        updatedAt = 1000L,
                        text = ""
                    )
                )
            )
        )
        var focusRequest by mutableStateOf<FocusRequest?>(null)

        composeTestRule.setContent {
            AshNotesTheme {
                EditorScreen(
                    uiState = UiState.Success(
                        EditorState(
                            noteId = "test-note",
                            title = "",
                            document = document,
                            saveStatus = SaveStatus.Saved,
                            canUndo = false,
                            canRedo = false,
                            focusRequest = focusRequest
                        )
                    ),
                    onBackClick = {},
                    onTitleChanged = {},
                    onParagraphTextChanged = { id, text ->
                        document = document.copy(
                            blocks = document.blocks.map { if (it.id == id && it is Block.Paragraph) it.copy(text = text) else it }
                        )
                    },
                    onHeadingTextChanged = { _, _ -> },
                    onChecklistTextChanged = { id, text ->
                        document = document.copy(
                            blocks = document.blocks.map { if (it.id == id && it is Block.Checklist) it.copy(text = text) else it }
                        )
                    },
                    onToggleChecklistChecked = { id ->
                        document = document.copy(
                            blocks = document.blocks.map { if (it.id == id && it is Block.Checklist) it.copy(isChecked = !it.isChecked) else it }
                        )
                    },
                    onSplitChecklist = { id, cursor ->
                        val result = ChecklistOperations.splitChecklistBlock(document.blocks, id, cursor)
                        document = document.copy(blocks = result.blocks)
                        focusRequest = result.focusBlockId?.let { FocusRequest(blockId = it, cursor = result.focusCursorIndex) }
                    },
                    onBackspaceInChecklist = { _, _ -> },
                    onPasteTextInChecklist = { id, text, cursor ->
                        val result = ChecklistOperations.pasteTextIntoChecklist(document.blocks, id, text, cursor)
                        document = document.copy(blocks = result.blocks)
                        focusRequest = result.focusBlockId?.let { FocusRequest(blockId = it, cursor = result.focusCursorIndex) }
                    },
                    onSplitParagraph = { _, _ -> },
                    onSplitHeading = { _, _ -> },
                    onPasteTextInParagraph = { _, _, _ -> },
                    onPasteTextInHeading = { _, _, _ -> },
                    onBackspaceInParagraph = { _, _ -> },
                    onBlockFocused = { _, _ -> },
                    onAppendParagraph = {},
                    onTapBelowBlocks = {},
                    onToggleChecklist = { id ->
                        val block = document.blocks.first { it.id == id }
                        val converted = ChecklistOperations.convertBlockToChecklist(block)
                        document = document.copy(
                            blocks = document.blocks.toMutableList().apply {
                                val idx = indexOfFirst { it.id == id }
                                removeAt(idx)
                                addAll(idx, converted)
                            }
                        )
                        focusRequest = converted.firstOrNull()?.id?.let { FocusRequest(blockId = it, cursor = 0) }
                    },
                    onUndoClick = {},
                    onRedoClick = {},
                    onFlushSave = {},
                    onClearFocusRequest = {
                        focusRequest = null
                    }
                )
            }
        }

        val checklistLabel = composeTestRule.activity.getString(R.string.toolbar_checklist)
        composeTestRule.onNodeWithContentDescription(checklistLabel).performClick()

        val checklistNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        checklistNodes.assertCountEquals(1)
        checklistNodes[0].assertIsFocused()
    }

    @Test
    fun typeText_pressEnterAtEnd_createsTwoChecklistNodes_firstHasExactText_secondEmptyAndFocused() {
        var document by mutableStateOf(
            Document(
                version = 1,
                blocks = listOf(
                    Block.Checklist(
                        id = "c1",
                        position = 0,
                        createdAt = 1000L,
                        updatedAt = 1000L,
                        text = "",
                        isChecked = false
                    )
                )
            )
        )
        var focusRequest by mutableStateOf<FocusRequest?>(FocusRequest("c1", 0))

        composeTestRule.setContent {
            AshNotesTheme {
                EditorScreen(
                    uiState = UiState.Success(
                        EditorState(
                            noteId = "test-note",
                            title = "",
                            document = document,
                            saveStatus = SaveStatus.Saved,
                            canUndo = false,
                            canRedo = false,
                            focusRequest = focusRequest
                        )
                    ),
                    onBackClick = {},
                    onTitleChanged = {},
                    onParagraphTextChanged = { _, _ -> },
                    onHeadingTextChanged = { _, _ -> },
                    onChecklistTextChanged = { id, text ->
                        document = document.copy(
                            blocks = document.blocks.map { if (it.id == id && it is Block.Checklist) it.copy(text = text) else it }
                        )
                    },
                    onToggleChecklistChecked = {},
                    onSplitChecklist = { id, cursor ->
                        val result = ChecklistOperations.splitChecklistBlock(document.blocks, id, cursor)
                        document = document.copy(blocks = result.blocks)
                        focusRequest = result.focusBlockId?.let { FocusRequest(blockId = it, cursor = result.focusCursorIndex) }
                    },
                    onBackspaceInChecklist = { _, _ -> },
                    onPasteTextInChecklist = { _, _, _ -> },
                    onSplitParagraph = { _, _ -> },
                    onSplitHeading = { _, _ -> },
                    onPasteTextInParagraph = { _, _, _ -> },
                    onPasteTextInHeading = { _, _, _ -> },
                    onBackspaceInParagraph = { _, _ -> },
                    onBlockFocused = { _, _ -> },
                    onAppendParagraph = {},
                    onTapBelowBlocks = {},
                    onToggleChecklist = { _ -> },
                    onUndoClick = {},
                    onRedoClick = {},
                    onFlushSave = {},
                    onClearFocusRequest = {
                        focusRequest = null
                    }
                )
            }
        }

        val checklistNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        checklistNodes[0].performTextInput("Buy Groceries")
        checklistNodes[0].performTextInput("\n")

        val updatedNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        updatedNodes.assertCountEquals(2)
        updatedNodes[0].assertTextEquals("Buy Groceries")
        updatedNodes[1].assertTextEquals("")
        updatedNodes[1].assertIsFocused()
    }

    @Test
    fun cursorAtIndex4_pressEnter_splitsIntoBuyAndMilk() {
        var document by mutableStateOf(
            Document(
                version = 1,
                blocks = listOf(
                    Block.Checklist(
                        id = "c1",
                        position = 0,
                        createdAt = 1000L,
                        updatedAt = 1000L,
                        text = "Buy milk",
                        isChecked = false
                    )
                )
            )
        )
        var focusRequest by mutableStateOf<FocusRequest?>(FocusRequest("c1", 4))

        composeTestRule.setContent {
            AshNotesTheme {
                EditorScreen(
                    uiState = UiState.Success(
                        EditorState(
                            noteId = "test-note",
                            title = "",
                            document = document,
                            saveStatus = SaveStatus.Saved,
                            canUndo = false,
                            canRedo = false,
                            focusRequest = focusRequest
                        )
                    ),
                    onBackClick = {},
                    onTitleChanged = {},
                    onParagraphTextChanged = { _, _ -> },
                    onHeadingTextChanged = { _, _ -> },
                    onChecklistTextChanged = { id, text ->
                        document = document.copy(
                            blocks = document.blocks.map { if (it.id == id && it is Block.Checklist) it.copy(text = text) else it }
                        )
                    },
                    onToggleChecklistChecked = {},
                    onSplitChecklist = { id, cursor ->
                        val result = ChecklistOperations.splitChecklistBlock(document.blocks, id, cursor)
                        document = document.copy(blocks = result.blocks)
                        focusRequest = result.focusBlockId?.let { FocusRequest(blockId = it, cursor = result.focusCursorIndex) }
                    },
                    onBackspaceInChecklist = { _, _ -> },
                    onPasteTextInChecklist = { _, _, _ -> },
                    onSplitParagraph = { _, _ -> },
                    onSplitHeading = { _, _ -> },
                    onPasteTextInParagraph = { _, _, _ -> },
                    onPasteTextInHeading = { _, _, _ -> },
                    onBackspaceInParagraph = { _, _ -> },
                    onBlockFocused = { _, _ -> },
                    onAppendParagraph = {},
                    onTapBelowBlocks = {},
                    onToggleChecklist = { _ -> },
                    onUndoClick = {},
                    onRedoClick = {},
                    onFlushSave = {},
                    onClearFocusRequest = {
                        focusRequest = null
                    }
                )
            }
        }

        val checklistNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        checklistNodes[0].performTextInput("\n")

        val updatedNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        updatedNodes.assertCountEquals(2)
        updatedNodes[0].assertTextEquals("Buy ")
        updatedNodes[1].assertTextEquals("milk")
    }

    @Test
    fun pasteMultiLineText_createsExactlyThreeItemsWithTextsA_B_C() {
        var document by mutableStateOf(
            Document(
                version = 1,
                blocks = listOf(
                    Block.Checklist(
                        id = "c1",
                        position = 0,
                        createdAt = 1000L,
                        updatedAt = 1000L,
                        text = "",
                        isChecked = false
                    )
                )
            )
        )
        var focusRequest by mutableStateOf<FocusRequest?>(FocusRequest("c1", 0))

        composeTestRule.setContent {
            AshNotesTheme {
                EditorScreen(
                    uiState = UiState.Success(
                        EditorState(
                            noteId = "test-note",
                            title = "",
                            document = document,
                            saveStatus = SaveStatus.Saved,
                            canUndo = false,
                            canRedo = false,
                            focusRequest = focusRequest
                        )
                    ),
                    onBackClick = {},
                    onTitleChanged = {},
                    onParagraphTextChanged = { _, _ -> },
                    onHeadingTextChanged = { _, _ -> },
                    onChecklistTextChanged = { id, text ->
                        document = document.copy(
                            blocks = document.blocks.map { if (it.id == id && it is Block.Checklist) it.copy(text = text) else it }
                        )
                    },
                    onToggleChecklistChecked = {},
                    onSplitChecklist = { _, _ -> },
                    onBackspaceInChecklist = { _, _ -> },
                    onPasteTextInChecklist = { id, text, cursor ->
                        val result = ChecklistOperations.pasteTextIntoChecklist(document.blocks, id, text, cursor)
                        document = document.copy(blocks = result.blocks)
                        focusRequest = result.focusBlockId?.let { FocusRequest(blockId = it, cursor = result.focusCursorIndex) }
                    },
                    onSplitParagraph = { _, _ -> },
                    onSplitHeading = { _, _ -> },
                    onPasteTextInParagraph = { _, _, _ -> },
                    onPasteTextInHeading = { _, _, _ -> },
                    onBackspaceInParagraph = { _, _ -> },
                    onBlockFocused = { _, _ -> },
                    onAppendParagraph = {},
                    onTapBelowBlocks = {},
                    onToggleChecklist = { _ -> },
                    onUndoClick = {},
                    onRedoClick = {},
                    onFlushSave = {},
                    onClearFocusRequest = {
                        focusRequest = null
                    }
                )
            }
        }

        val checklistNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        checklistNodes[0].performTextInput("a\nb\nc")

        val updatedNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        updatedNodes.assertCountEquals(3)
        updatedNodes[0].assertTextEquals("a")
        updatedNodes[1].assertTextEquals("b")
        updatedNodes[2].assertTextEquals("c")
    }

    @Test
    fun tapCheckbox_onlyThatItemIsChecked() {
        var document by mutableStateOf(
            Document(
                version = 1,
                blocks = listOf(
                    Block.Checklist(
                        id = "c1",
                        position = 0,
                        createdAt = 1000L,
                        updatedAt = 1000L,
                        text = "Item 1",
                        isChecked = false
                    ),
                    Block.Checklist(
                        id = "c2",
                        position = 1,
                        createdAt = 1000L,
                        updatedAt = 1000L,
                        text = "Item 2",
                        isChecked = false
                    )
                )
            )
        )

        composeTestRule.setContent {
            AshNotesTheme {
                EditorScreen(
                    uiState = UiState.Success(
                        EditorState(
                            noteId = "test-note",
                            title = "",
                            document = document,
                            saveStatus = SaveStatus.Saved,
                            canUndo = false,
                            canRedo = false,
                            focusRequest = null
                        )
                    ),
                    onBackClick = {},
                    onTitleChanged = {},
                    onParagraphTextChanged = { _, _ -> },
                    onHeadingTextChanged = { _, _ -> },
                    onChecklistTextChanged = { _, _ -> },
                    onToggleChecklistChecked = { id ->
                        document = document.copy(
                            blocks = document.blocks.map {
                                if (it.id == id && it is Block.Checklist) it.copy(isChecked = !it.isChecked) else it
                            }
                        )
                    },
                    onSplitChecklist = { _, _ -> },
                    onBackspaceInChecklist = { _, _ -> },
                    onPasteTextInChecklist = { _, _, _ -> },
                    onSplitParagraph = { _, _ -> },
                    onSplitHeading = { _, _ -> },
                    onPasteTextInParagraph = { _, _, _ -> },
                    onPasteTextInHeading = { _, _, _ -> },
                    onBackspaceInParagraph = { _, _ -> },
                    onBlockFocused = { _, _ -> },
                    onAppendParagraph = {},
                    onTapBelowBlocks = {},
                    onToggleChecklist = { _ -> },
                    onUndoClick = {},
                    onRedoClick = {},
                    onFlushSave = {},
                    onClearFocusRequest = {}
                )
            }
        }

        val checkboxNodes = composeTestRule.onAllNodesWithTag("checklist_checkbox")
        checkboxNodes[0].performClick()

        val updatedCheckboxNodes = composeTestRule.onAllNodesWithTag("checklist_checkbox")
        updatedCheckboxNodes[0].assertIsSelected()
        updatedCheckboxNodes[1].assertIsNotSelected()
    }

    @Test
    fun typeFastOneCharTime_resultIsExactString() {
        var document by mutableStateOf(
            Document(
                version = 1,
                blocks = listOf(
                    Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
                )
            )
        )
        var focusRequest by mutableStateOf<FocusRequest?>(FocusRequest("p1", 0))

        composeTestRule.setContent {
            AshNotesTheme {
                EditorScreen(
                    uiState = UiState.Success(
                        EditorState(
                            noteId = "test-note",
                            title = "",
                            document = document,
                            saveStatus = SaveStatus.Saved,
                            canUndo = false,
                            canRedo = false,
                            focusRequest = focusRequest
                        )
                    ),
                    onBackClick = {},
                    onTitleChanged = {},
                    onParagraphTextChanged = { id, text ->
                        document = document.copy(
                            blocks = document.blocks.map { if (it.id == id && it is Block.Paragraph) it.copy(text = text) else it }
                        )
                    },
                    onHeadingTextChanged = { _, _ -> },
                    onChecklistTextChanged = { _, _ -> },
                    onToggleChecklistChecked = {},
                    onSplitChecklist = { _, _ -> },
                    onBackspaceInChecklist = { _, _ -> },
                    onPasteTextInChecklist = { _, _, _ -> },
                    onSplitParagraph = { _, _ -> },
                    onSplitHeading = { _, _ -> },
                    onPasteTextInParagraph = { _, _, _ -> },
                    onPasteTextInHeading = { _, _, _ -> },
                    onBackspaceInParagraph = { _, _ -> },
                    onBlockFocused = { _, _ -> },
                    onAppendParagraph = {},
                    onTapBelowBlocks = {},
                    onToggleChecklist = { _ -> },
                    onUndoClick = {},
                    onRedoClick = {},
                    onFlushSave = {},
                    onClearFocusRequest = { focusRequest = null }
                )
            }
        }

        val paragraphNodes = composeTestRule.onAllNodesWithTag("paragraph_text")
        "abcdef".forEach { char ->
            paragraphNodes[0].performTextInput(char.toString())
        }

        val updatedNodes = composeTestRule.onAllNodesWithTag("paragraph_text")
        updatedNodes[0].assertTextEquals("abcdef")
    }

    @Test
    fun caretAtIndex3_pressEnter_splitsIntoAbcAndDef_focusesSecondAtIndex0() {
        var document by mutableStateOf(
            Document(
                version = 1,
                blocks = listOf(
                    Block.Checklist(id = "c1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "abcdef", isChecked = false)
                )
            )
        )
        var focusRequest by mutableStateOf<FocusRequest?>(FocusRequest("c1", 3))

        composeTestRule.setContent {
            AshNotesTheme {
                EditorScreen(
                    uiState = UiState.Success(
                        EditorState(
                            noteId = "test-note",
                            title = "",
                            document = document,
                            saveStatus = SaveStatus.Saved,
                            canUndo = false,
                            canRedo = false,
                            focusRequest = focusRequest
                        )
                    ),
                    onBackClick = {},
                    onTitleChanged = {},
                    onParagraphTextChanged = { _, _ -> },
                    onHeadingTextChanged = { _, _ -> },
                    onChecklistTextChanged = { id, text ->
                        document = document.copy(
                            blocks = document.blocks.map { if (it.id == id && it is Block.Checklist) it.copy(text = text) else it }
                        )
                    },
                    onToggleChecklistChecked = {},
                    onSplitChecklist = { id, cursor ->
                        val result = ChecklistOperations.splitChecklistBlock(document.blocks, id, cursor)
                        document = document.copy(blocks = result.blocks)
                        focusRequest = result.focusBlockId?.let { FocusRequest(blockId = it, cursor = result.focusCursorIndex) }
                    },
                    onBackspaceInChecklist = { _, _ -> },
                    onPasteTextInChecklist = { _, _, _ -> },
                    onSplitParagraph = { _, _ -> },
                    onSplitHeading = { _, _ -> },
                    onPasteTextInParagraph = { _, _, _ -> },
                    onPasteTextInHeading = { _, _, _ -> },
                    onBackspaceInParagraph = { _, _ -> },
                    onBlockFocused = { _, _ -> },
                    onAppendParagraph = {},
                    onTapBelowBlocks = {},
                    onToggleChecklist = { _ -> },
                    onUndoClick = {},
                    onRedoClick = {},
                    onFlushSave = {},
                    onClearFocusRequest = { focusRequest = null }
                )
            }
        }

        val checklistNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        checklistNodes[0].performTextInputSelection(TextRange(3))
        checklistNodes[0].performTextInput("\n")

        val updatedNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        updatedNodes.assertCountEquals(2)
        updatedNodes[0].assertTextEquals("abc")
        updatedNodes[1].assertTextEquals("def")
        updatedNodes[1].assertIsFocused()
    }

    @Test
    fun list30Items_enterOnItem20_doesNotScrollItem20ToTop() {
        val initialBlocks = (1..30).map { i ->
            Block.Checklist(id = "c$i", position = i - 1, createdAt = 1000L, updatedAt = 1000L, text = "Item $i", isChecked = false)
        }
        var document by mutableStateOf(Document(version = 1, blocks = initialBlocks))
        var focusRequest by mutableStateOf<FocusRequest?>(FocusRequest("c20", 0))

        composeTestRule.setContent {
            AshNotesTheme {
                EditorScreen(
                    uiState = UiState.Success(
                        EditorState(
                            noteId = "test-note",
                            title = "",
                            document = document,
                            saveStatus = SaveStatus.Saved,
                            canUndo = false,
                            canRedo = false,
                            focusRequest = focusRequest
                        )
                    ),
                    onBackClick = {},
                    onTitleChanged = {},
                    onParagraphTextChanged = { _, _ -> },
                    onHeadingTextChanged = { _, _ -> },
                    onChecklistTextChanged = { id, text ->
                        document = document.copy(
                            blocks = document.blocks.map { if (it.id == id && it is Block.Checklist) it.copy(text = text) else it }
                        )
                    },
                    onToggleChecklistChecked = {},
                    onSplitChecklist = { id, cursor ->
                        val result = ChecklistOperations.splitChecklistBlock(document.blocks, id, cursor)
                        document = document.copy(blocks = result.blocks)
                        focusRequest = result.focusBlockId?.let { FocusRequest(blockId = it, cursor = result.focusCursorIndex) }
                    },
                    onBackspaceInChecklist = { _, _ -> },
                    onPasteTextInChecklist = { _, _, _ -> },
                    onSplitParagraph = { _, _ -> },
                    onSplitHeading = { _, _ -> },
                    onPasteTextInParagraph = { _, _, _ -> },
                    onPasteTextInHeading = { _, _, _ -> },
                    onBackspaceInParagraph = { _, _ -> },
                    onBlockFocused = { _, _ -> },
                    onAppendParagraph = {},
                    onTapBelowBlocks = {},
                    onToggleChecklist = { _ -> },
                    onUndoClick = {},
                    onRedoClick = {},
                    onFlushSave = {},
                    onClearFocusRequest = { focusRequest = null }
                )
            }
        }

        val targetItemNode = composeTestRule.onAllNodesWithTag("checklist_text")[19]
        targetItemNode.performTextInput("\n")

        val updatedNodes = composeTestRule.onAllNodesWithTag("checklist_text")
        updatedNodes.assertCountEquals(31)
        // Item 20 split into two items; node at index 20 (new item) is focused
        updatedNodes[20].assertIsFocused()
    }
}
