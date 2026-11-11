package com.rayven0421.ashnotes.feature.editor

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rayven0421.ashnotes.BuildConfig
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

private suspend fun safeRequestBlockFocus(
    focusId: String,
    targetIndex: Int,
    lazyListState: LazyListState,
    focusRequesters: Map<String, FocusRequester>,
    keyboardController: SoftwareKeyboardController?
) {
    val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
    val isVisible = visibleItems.any { it.key == focusId || it.index == targetIndex }
    if (!isVisible && targetIndex >= 0) {
        val firstVisible = visibleItems.firstOrNull()?.index ?: 0
        if (targetIndex < firstVisible) {
            lazyListState.animateScrollToItem(targetIndex)
        } else {
            lazyListState.animateScrollToItem(targetIndex)
        }
    }
    withTimeoutOrNull(1000L) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.any { it.key == focusId } }
            .first { it }
    }
    withFrameNanos { }
    val requester = focusRequesters[focusId] ?: return
    try {
        requester.requestFocus()
        keyboardController?.show()
    } catch (_: IllegalStateException) {
        try {
            withFrameNanos { }
            requester.requestFocus()
            keyboardController?.show()
        } catch (e2: IllegalStateException) {
            if (BuildConfig.DEBUG) {
                Log.w("EditorScreen", "Failed to request focus for block $focusId", e2)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    uiState: UiState<EditorState>,
    onBackClick: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onParagraphTextChanged: (String, String) -> Unit,
    onHeadingTextChanged: (String, String) -> Unit,
    onChecklistTextChanged: (String, String) -> Unit,
    onToggleChecklistChecked: (String) -> Unit,
    onSplitChecklist: (String, Int) -> Unit,
    onBackspaceInChecklist: (String, Int) -> Unit,
    onPasteTextInChecklist: (String, String, Int) -> Unit,
    onSplitParagraph: (String, Int) -> Unit,
    onSplitHeading: (String, Int) -> Unit,
    onPasteTextInParagraph: (String, String, Int) -> Unit,
    onPasteTextInHeading: (String, String, Int) -> Unit,
    onBackspaceInParagraph: (String, Int) -> Unit,
    onDeleteBlock: (String) -> Unit = {},
    onInsertBlockBelow: (kotlin.reflect.KClass<out com.rayven0421.ashnotes.domain.model.TextBlock>) -> Unit = {},
    onInsertDivider: () -> Unit = {},
    onBlockFocused: (String, Int) -> Unit,
    onAppendParagraph: () -> Unit,
    onTapBelowBlocks: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onFlushSave: () -> Unit,
    onClearFocusRequest: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen = WindowInsets.isImeVisible

    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    var focusedBlockId by remember { mutableStateOf<String?>(null) }
    var pendingAppendFocus by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                onFlushSave()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onFlushSave()
        }
    }

    BackHandler {
        onFlushSave()
        onBackClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AshNotesTheme.colors.ember)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        color = AshNotesTheme.colors.over,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            UiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.notes_empty_message),
                        color = AshNotesTheme.colors.textMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is UiState.Success -> {
                val state = uiState.data
                val blocks = state.document.blocks

                LaunchedEffect(state.focusRequest?.token) {
                    val request = state.focusRequest
                    if (request != null) {
                        val targetIndex = blocks.indexOfFirst { it.id == request.blockId }
                        safeRequestBlockFocus(
                            focusId = request.blockId,
                            targetIndex = targetIndex,
                            lazyListState = lazyListState,
                            focusRequesters = focusRequesters,
                            keyboardController = keyboardController
                        )
                        onClearFocusRequest(request.token)
                    }
                }

                LaunchedEffect(blocks.size, pendingAppendFocus) {
                    if (pendingAppendFocus) {
                        pendingAppendFocus = false
                        val lastId = blocks.lastOrNull()?.id
                        if (lastId != null) {
                            val targetIndex = blocks.size - 1
                            safeRequestBlockFocus(
                                focusId = lastId,
                                targetIndex = targetIndex,
                                lazyListState = lazyListState,
                                focusRequesters = focusRequesters,
                                keyboardController = keyboardController
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = AshNotesTheme.spacing.screenHorizontal)
                    ) {
                        EditorHeader(
                            saveStatus = state.saveStatus,
                            errorMessage = state.errorMessage,
                            canUndo = state.canUndo,
                            canRedo = state.canRedo,
                            isKeyboardOpen = isKeyboardOpen,
                            onBackClick = {
                                onFlushSave()
                                onBackClick()
                            },
                            onUndoClick = onUndoClick,
                            onRedoClick = onRedoClick,
                            onDoneClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        )

                        Spacer(modifier = Modifier.height(AshNotesTheme.spacing.small))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .widthIn(max = EditorTokens.ContentMaxWidth)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            TitleTextField(
                                title = state.title,
                                onTitleChanged = onTitleChanged,
                                onDone = {
                                    val firstBlockId = blocks.firstOrNull()?.id
                                    if (firstBlockId != null) {
                                        focusRequesters[firstBlockId]?.requestFocus()
                                        keyboardController?.show()
                                    } else {
                                        pendingAppendFocus = true
                                        onAppendParagraph()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(AshNotesTheme.spacing.small))

                            EditorBlockList(
                                blocks = blocks,
                                lazyListState = lazyListState,
                                focusRequesters = focusRequesters,
                                focusRequest = state.focusRequest,
                                onParagraphTextChanged = onParagraphTextChanged,
                                onHeadingTextChanged = onHeadingTextChanged,
                                onChecklistTextChanged = onChecklistTextChanged,
                                onToggleChecklistChecked = onToggleChecklistChecked,
                                onSplitChecklist = onSplitChecklist,
                                onBackspaceInChecklist = onBackspaceInChecklist,
                                onPasteTextInChecklist = onPasteTextInChecklist,
                                onSplitParagraph = onSplitParagraph,
                                onSplitHeading = onSplitHeading,
                                onPasteTextInParagraph = onPasteTextInParagraph,
                                onPasteTextInHeading = onPasteTextInHeading,
                                onBackspaceInParagraph = onBackspaceInParagraph,
                                onDeleteBlock = onDeleteBlock,
                                onBlockFocused = onBlockFocused,
                                onTapBelowBlocks = onTapBelowBlocks,
                                onBlockFocusChanged = { focusedBlockId = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }

                    // Floating bottom toolbar
                    val activeBlock = blocks.firstOrNull { it.id == focusedBlockId } ?: blocks.lastOrNull()
                    val targetBlockId = activeBlock?.id

                    if (targetBlockId != null) {
                        EditorBottomToolbar(
                            onInsertBlock = onInsertBlockBelow,
                            onInsertDivider = onInsertDivider
                        )
                    }
                }
            }
        }
    }
}
