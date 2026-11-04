package com.rayven0421.ashnotes.feature.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.Block

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
    onBackspaceInParagraph: (String, Int) -> Unit,
    onBlockFocused: (String, Int) -> Unit,
    onAppendParagraph: () -> Unit,
    onToggleChecklist: (String) -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onFlushSave: () -> Unit,
    onClearPendingFocus: () -> Unit,
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

                LaunchedEffect(state.pendingFocusBlockId, state.pendingFocusCursorIndex) {
                    val focusId = state.pendingFocusBlockId
                    if (focusId != null) {
                        val targetIndex = blocks.indexOfFirst { it.id == focusId }
                        if (targetIndex >= 0) {
                            lazyListState.animateScrollToItem(targetIndex)
                        }
                        val focusRequester = focusRequesters[focusId]
                        focusRequester?.requestFocus()
                        keyboardController?.show()
                        onClearPendingFocus()
                    }
                }

                LaunchedEffect(blocks.size) {
                    if (pendingAppendFocus) {
                        pendingAppendFocus = false
                        val lastId = blocks.lastOrNull()?.id
                        if (lastId != null) {
                            val targetIndex = blocks.size - 1
                            if (targetIndex >= 0) {
                                lazyListState.animateScrollToItem(targetIndex)
                            }
                            focusRequesters[lastId]?.requestFocus()
                            keyboardController?.show()
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
                                .widthIn(max = 680.dp)
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

                            val tapInteractionSource = remember { MutableInteractionSource() }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                LazyColumn(
                                    state = lazyListState,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
                                ) {
                                    items(blocks, key = { it.id }) { block ->
                                        val focusRequester = focusRequesters.getOrPut(block.id) { FocusRequester() }
                                        val blockModifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)

                                        when (block) {
                                            is Block.Paragraph -> {
                                                val isOnlyBlockAndEmpty = blocks.size == 1 && block.text.isEmpty()
                                                ParagraphBlockItem(
                                                    block = block,
                                                    isOnlyBlockAndEmpty = isOnlyBlockAndEmpty,
                                                    pendingFocusBlockId = state.pendingFocusBlockId,
                                                    pendingFocusCursorIndex = state.pendingFocusCursorIndex,
                                                    modifier = blockModifier,
                                                    onTextChanged = { newText ->
                                                        onParagraphTextChanged(block.id, newText)
                                                    },
                                                    onEnterPressed = { cursorIndex ->
                                                        onSplitParagraph(block.id, cursorIndex)
                                                    },
                                                    onBackspaceAtStart = {
                                                        onBackspaceInParagraph(block.id, 0)
                                                    },
                                                    onFocused = { cursorIndex ->
                                                        focusedBlockId = block.id
                                                        onBlockFocused(block.id, cursorIndex)
                                                    }
                                                )
                                            }
                                            is Block.Heading -> {
                                                HeadingBlockItem(
                                                    block = block,
                                                    pendingFocusBlockId = state.pendingFocusBlockId,
                                                    pendingFocusCursorIndex = state.pendingFocusCursorIndex,
                                                    modifier = blockModifier,
                                                    onTextChanged = { newText ->
                                                        onHeadingTextChanged(block.id, newText)
                                                    },
                                                    onEnterPressed = { cursorIndex ->
                                                        onSplitParagraph(block.id, cursorIndex)
                                                    },
                                                    onBackspaceAtStart = {
                                                        onBackspaceInParagraph(block.id, 0)
                                                    },
                                                    onFocused = { cursorIndex ->
                                                        focusedBlockId = block.id
                                                        onBlockFocused(block.id, cursorIndex)
                                                    }
                                                )
                                            }
                                            is Block.Checklist -> {
                                                ChecklistBlockItem(
                                                    block = block,
                                                    pendingFocusBlockId = state.pendingFocusBlockId,
                                                    pendingFocusCursorIndex = state.pendingFocusCursorIndex,
                                                    modifier = blockModifier,
                                                    onToggleChecked = {
                                                        onToggleChecklistChecked(block.id)
                                                    },
                                                    onTextChanged = { newText ->
                                                        onChecklistTextChanged(block.id, newText)
                                                    },
                                                    onEnterPressed = { cursorIndex ->
                                                        onSplitChecklist(block.id, cursorIndex)
                                                    },
                                                    onBackspaceAtStart = {
                                                        onBackspaceInChecklist(block.id, 0)
                                                    },
                                                    onPasteMultiLine = { pastedText, cursorIndex ->
                                                        onPasteTextInChecklist(block.id, pastedText, cursorIndex)
                                                    },
                                                    onFocused = { cursorIndex ->
                                                        focusedBlockId = block.id
                                                        onBlockFocused(block.id, cursorIndex)
                                                    }
                                                )
                                            }
                                            is Block.UnknownBlock -> {
                                                Text(
                                                    text = stringResource(R.string.block_unknown_fallback, block.rawType),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AshNotesTheme.colors.textMuted
                                                )
                                            }
                                        }
                                    }
                                }

                                // Tap-to-focus invisible area below blocks
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clickable(
                                            interactionSource = tapInteractionSource,
                                            indication = null
                                        ) {
                                            val lastBlock = blocks.lastOrNull()
                                            when (lastBlock) {
                                                is Block.Paragraph -> {
                                                    focusRequesters[lastBlock.id]?.requestFocus()
                                                    keyboardController?.show()
                                                }
                                                else -> {
                                                    pendingAppendFocus = true
                                                    onAppendParagraph()
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    }

                    // Floating bottom toolbar
                    val activeBlock = blocks.firstOrNull { it.id == focusedBlockId } ?: blocks.lastOrNull()
                    val isChecklistActive = activeBlock is Block.Checklist
                    val targetBlockId = activeBlock?.id

                    if (targetBlockId != null) {
                        EditorBottomToolbar(
                            isChecklistActive = isChecklistActive,
                            onToggleChecklist = { onToggleChecklist(targetBlockId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorHeader(
    saveStatus: SaveStatus,
    errorMessage: String?,
    canUndo: Boolean,
    canRedo: Boolean,
    isKeyboardOpen: Boolean,
    onBackClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AshNotesTheme.colors.raised,
                border = BorderStroke(1.dp, AshNotesTheme.colors.line),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onUndoClick,
                        enabled = canUndo,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Undo,
                            contentDescription = stringResource(R.string.action_undo),
                            tint = if (canUndo) MaterialTheme.colorScheme.onBackground else AshNotesTheme.colors.textMuted.copy(alpha = 0.4f)
                        )
                    }

                    IconButton(
                        onClick = onRedoClick,
                        enabled = canRedo,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Redo,
                            contentDescription = stringResource(R.string.action_redo),
                            tint = if (canRedo) MaterialTheme.colorScheme.onBackground else AshNotesTheme.colors.textMuted.copy(alpha = 0.4f)
                        )
                    }

                    Text(
                        text = when (saveStatus) {
                            SaveStatus.Saved -> stringResource(R.string.editor_status_saved)
                            SaveStatus.Saving -> stringResource(R.string.editor_status_saving)
                            SaveStatus.Error -> errorMessage ?: stringResource(R.string.editor_status_error)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (saveStatus) {
                            SaveStatus.Saved -> AshNotesTheme.colors.textMuted
                            SaveStatus.Saving -> AshNotesTheme.colors.ember
                            SaveStatus.Error -> AshNotesTheme.colors.over
                        },
                        modifier = Modifier.padding(end = 10.dp, start = 4.dp)
                    )
                }
            }

            if (isKeyboardOpen) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AshNotesTheme.colors.ember)
                        .clickable(onClick = onDoneClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Done,
                        contentDescription = stringResource(R.string.action_done),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockContainer(
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(
                    if (isFocused) AshNotesTheme.colors.ember else Color.Transparent,
                    RoundedCornerShape(1.dp)
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopStart
        ) {
            content()
        }
    }
}

@Composable
private fun TitleTextField(
    title: String,
    onTitleChanged: (String) -> Unit,
    onDone: () -> Unit
) {
    BasicTextField(
        value = title,
        onValueChange = onTitleChanged,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.displayMedium.copy(
            color = MaterialTheme.colorScheme.onBackground
        ),
        cursorBrush = SolidColor(AshNotesTheme.colors.ember),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        maxLines = 2,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (title.isEmpty()) {
                    Text(
                        text = stringResource(R.string.editor_title_placeholder),
                        style = MaterialTheme.typography.displayMedium,
                        color = AshNotesTheme.colors.textMuted
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun ParagraphBlockItem(
    block: Block.Paragraph,
    isOnlyBlockAndEmpty: Boolean,
    pendingFocusBlockId: String?,
    pendingFocusCursorIndex: Int,
    onTextChanged: (String) -> Unit,
    onEnterPressed: (Int) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onFocused: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember(block.id) {
        mutableStateOf(
            TextFieldValue(
                text = block.text,
                selection = TextRange(
                    if (block.id == pendingFocusBlockId) pendingFocusCursorIndex.coerceIn(0, block.text.length) else block.text.length
                )
            )
        )
    }

    LaunchedEffect(block.text, pendingFocusCursorIndex, isFocused) {
        if (textFieldValue.text != block.text) {
            val targetCursor = if (isFocused && pendingFocusCursorIndex >= 0) {
                pendingFocusCursorIndex.coerceIn(0, block.text.length)
            } else {
                textFieldValue.selection.start.coerceIn(0, block.text.length)
            }
            textFieldValue = TextFieldValue(
                text = block.text,
                selection = TextRange(targetCursor)
            )
        }
    }

    BlockContainer(
        isFocused = isFocused,
        modifier = modifier
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                if (newValue.text != block.text) {
                    onTextChanged(newValue.text)
                }
                onFocused(newValue.selection.start)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (focusState.isFocused) {
                        onFocused(textFieldValue.selection.start)
                    }
                }
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        if (keyEvent.key == Key.Enter) {
                            onEnterPressed(textFieldValue.selection.start)
                            true
                        } else if (keyEvent.key == Key.Backspace && textFieldValue.selection.start == 0) {
                            onBackspaceAtStart()
                            true
                        } else false
                    } else false
                },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            cursorBrush = SolidColor(AshNotesTheme.colors.ember),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (isOnlyBlockAndEmpty && block.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.editor_paragraph_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AshNotesTheme.colors.textMuted.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun HeadingBlockItem(
    block: Block.Heading,
    pendingFocusBlockId: String?,
    pendingFocusCursorIndex: Int,
    onTextChanged: (String) -> Unit,
    onEnterPressed: (Int) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onFocused: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember(block.id) {
        mutableStateOf(
            TextFieldValue(
                text = block.text,
                selection = TextRange(
                    if (block.id == pendingFocusBlockId) pendingFocusCursorIndex.coerceIn(0, block.text.length) else block.text.length
                )
            )
        )
    }

    LaunchedEffect(block.text, pendingFocusCursorIndex, isFocused) {
        if (textFieldValue.text != block.text) {
            val targetCursor = if (isFocused && pendingFocusCursorIndex >= 0) {
                pendingFocusCursorIndex.coerceIn(0, block.text.length)
            } else {
                textFieldValue.selection.start.coerceIn(0, block.text.length)
            }
            textFieldValue = TextFieldValue(
                text = block.text,
                selection = TextRange(targetCursor)
            )
        }
    }

    BlockContainer(
        isFocused = isFocused,
        modifier = modifier
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                if (newValue.text != block.text) {
                    onTextChanged(newValue.text)
                }
                onFocused(newValue.selection.start)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (focusState.isFocused) {
                        onFocused(textFieldValue.selection.start)
                    }
                }
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        if (keyEvent.key == Key.Enter) {
                            onEnterPressed(textFieldValue.selection.start)
                            true
                        } else if (keyEvent.key == Key.Backspace && textFieldValue.selection.start == 0) {
                            onBackspaceAtStart()
                            true
                        } else false
                    } else false
                },
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            cursorBrush = SolidColor(AshNotesTheme.colors.ember),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (block.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.editor_heading_placeholder),
                            style = MaterialTheme.typography.titleLarge,
                            color = AshNotesTheme.colors.textMuted.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun ChecklistBlockItem(
    block: Block.Checklist,
    pendingFocusBlockId: String?,
    pendingFocusCursorIndex: Int,
    onToggleChecked: () -> Unit,
    onTextChanged: (String) -> Unit,
    onEnterPressed: (Int) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onPasteMultiLine: (String, Int) -> Unit,
    onFocused: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember(block.id) {
        mutableStateOf(
            TextFieldValue(
                text = block.text,
                selection = TextRange(
                    if (block.id == pendingFocusBlockId) pendingFocusCursorIndex.coerceIn(0, block.text.length) else block.text.length
                )
            )
        )
    }

    LaunchedEffect(block.text, pendingFocusCursorIndex, isFocused) {
        if (textFieldValue.text != block.text) {
            val targetCursor = if (isFocused && pendingFocusCursorIndex >= 0) {
                pendingFocusCursorIndex.coerceIn(0, block.text.length)
            } else {
                textFieldValue.selection.start.coerceIn(0, block.text.length)
            }
            textFieldValue = TextFieldValue(
                text = block.text,
                selection = TextRange(targetCursor)
            )
        }
    }

    BlockContainer(
        isFocused = isFocused,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val contentDesc = stringResource(if (block.isChecked) R.string.checkbox_mark_not_done else R.string.checkbox_mark_done)

            Box(
                modifier = Modifier
                    .offset(x = (-15).dp)
                    .size(48.dp)
                    .padding(top = 3.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, radius = 24.dp),
                        onClick = onToggleChecked
                    )
                    .semantics {
                        selected = block.isChecked
                        contentDescription = contentDesc
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (block.isChecked) AshNotesTheme.colors.ember else Color.Transparent)
                        .then(
                            if (block.isChecked) Modifier
                            else Modifier.border(1.dp, AshNotesTheme.colors.line, RoundedCornerShape(4.dp))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (block.isChecked) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(AshNotesTheme.spacing.extraSmall))

            val textStyle = if (block.isChecked) {
                MaterialTheme.typography.bodyLarge.copy(
                    color = AshNotesTheme.colors.textMuted,
                    textDecoration = TextDecoration.LineThrough
                )
            } else {
                MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.contains("\n")) {
                        val pasteCursor = textFieldValue.selection.start
                        onPasteMultiLine(newValue.text, pasteCursor)
                    } else {
                        textFieldValue = newValue
                        if (newValue.text != block.text) {
                            onTextChanged(newValue.text)
                        }
                        onFocused(newValue.selection.start)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (focusState.isFocused) {
                            onFocused(textFieldValue.selection.start)
                        }
                    }
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            if (keyEvent.key == Key.Enter) {
                                onEnterPressed(textFieldValue.selection.start)
                                true
                            } else if (keyEvent.key == Key.Backspace && textFieldValue.selection.start == 0) {
                                onBackspaceAtStart()
                                true
                            } else false
                        } else false
                    },
                textStyle = textStyle,
                cursorBrush = SolidColor(AshNotesTheme.colors.ember),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun EditorBottomToolbar(
    isChecklistActive: Boolean,
    onToggleChecklist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(
                horizontal = AshNotesTheme.spacing.screenHorizontal,
                vertical = AshNotesTheme.spacing.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = AshNotesTheme.colors.raised,
            border = BorderStroke(1.dp, AshNotesTheme.colors.line),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.widthIn(max = 680.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(52.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ToolbarIconButton(
                    icon = Icons.Outlined.CheckBox,
                    contentDescription = stringResource(R.string.toolbar_checklist),
                    isActive = isChecklistActive,
                    onClick = onToggleChecklist
                )
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = if (isActive) AshNotesTheme.colors.emberSoft else Color.Transparent
    val iconColor = if (isActive) AshNotesTheme.colors.ember else AshNotesTheme.colors.textMuted

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 24.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
