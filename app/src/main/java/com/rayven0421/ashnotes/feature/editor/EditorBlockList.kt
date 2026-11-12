package com.rayven0421.ashnotes.feature.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.Block
import com.rayven0421.ashnotes.domain.model.FocusRequest
import com.rayven0421.ashnotes.domain.model.TextBlock
import com.rayven0421.ashnotes.domain.model.TextBlockOperations
import com.rayven0421.ashnotes.feature.editor.blocks.DividerBlockItem

@Composable
fun EditorBlockList(
    blocks: List<Block>,
    lazyListState: LazyListState,
    focusRequesters: MutableMap<String, FocusRequester>,
    focusRequest: FocusRequest?,
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
    onDeleteBlock: (String) -> Unit,
    onBlockFocused: (String, Int) -> Unit,
    onTapBelowBlocks: () -> Unit,
    onBlockFocusChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tapInteractionSource = remember { MutableInteractionSource() }

    val handleBackspaceAtStart: (String, (String, Int) -> Unit) -> Unit = { blockId, onBackspaceCall ->
        val result = TextBlockOperations.mergeOnBackspace(blocks, blockId, 0)
        if (result.blocks.size < blocks.size) {
            val targetFocusId = result.focusBlockId
            if (targetFocusId != null && targetFocusId != blockId) {
                try {
                    focusRequesters[targetFocusId]?.requestFocus()
                } catch (_: IllegalStateException) {
                }
            }
        }
        onBackspaceCall(blockId, 0)
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
        ) {
            itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
                val focusRequester = focusRequesters.getOrPut(block.id) { FocusRequester() }
                val blockModifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                val itemFocusRequest = if (focusRequest?.blockId == block.id) focusRequest else null

                when (block) {
                    is TextBlock -> {
                        val isOnlyBlockAndEmpty = blocks.size == 1 && block is Block.Paragraph && block.text.isEmpty()
                        val number = if (block is Block.NumberedItem) TextBlockOperations.numberFor(blocks, index) else 0

                        val onTextChanged: (String) -> Unit
                        val onSplit: (Int) -> Unit
                        val onBackspaceAtStartCall: () -> Unit
                        val onPaste: (String, Int) -> Unit
                        val onToggleChecked: (() -> Unit)?

                        when (block) {
                            is Block.Checklist -> {
                                onTextChanged = { newText -> onChecklistTextChanged(block.id, newText) }
                                onSplit = { cursorIndex -> onSplitChecklist(block.id, cursorIndex) }
                                onBackspaceAtStartCall = { handleBackspaceAtStart(block.id, onBackspaceInChecklist) }
                                onPaste = { pastedText, cursorIndex -> onPasteTextInChecklist(block.id, pastedText, cursorIndex) }
                                onToggleChecked = { onToggleChecklistChecked(block.id) }
                            }
                            is Block.Heading -> {
                                onTextChanged = { newText -> onHeadingTextChanged(block.id, newText) }
                                onSplit = { cursorIndex -> onSplitHeading(block.id, cursorIndex) }
                                onBackspaceAtStartCall = { handleBackspaceAtStart(block.id, onBackspaceInParagraph) }
                                onPaste = { pastedText, cursorIndex -> onPasteTextInHeading(block.id, pastedText, cursorIndex) }
                                onToggleChecked = null
                            }
                            else -> {
                                onTextChanged = { newText -> onParagraphTextChanged(block.id, newText) }
                                onSplit = { cursorIndex -> onSplitParagraph(block.id, cursorIndex) }
                                onBackspaceAtStartCall = { handleBackspaceAtStart(block.id, onBackspaceInParagraph) }
                                onPaste = { pastedText, cursorIndex -> onPasteTextInParagraph(block.id, pastedText, cursorIndex) }
                                onToggleChecked = null
                            }
                        }

                        TextBlockRow(
                            block = block,
                            modifier = blockModifier,
                            number = number,
                            isOnlyBlockAndEmpty = isOnlyBlockAndEmpty,
                            focusRequest = itemFocusRequest,
                            onTextChanged = onTextChanged,
                            onEnterPressed = onSplit,
                            onBackspaceAtStart = onBackspaceAtStartCall,
                            onPasteMultiLine = onPaste,
                            onFocused = { cursorIndex ->
                                onBlockFocusChanged(block.id)
                                onBlockFocused(block.id, cursorIndex)
                            },
                            onToggleChecked = onToggleChecked
                        )
                    }
                    is Block.Divider -> {
                        DividerBlockItem(
                            block = block,
                            onSelect = {
                                focusRequester.requestFocus()
                            },
                            onDelete = {
                                val result = TextBlockOperations.deleteBlock(blocks, block.id)
                                if (result.blocks.size < blocks.size) {
                                    val targetFocusId = result.focusBlockId
                                    if (targetFocusId != null && targetFocusId != block.id) {
                                        try {
                                            focusRequesters[targetFocusId]?.requestFocus()
                                        } catch (_: IllegalStateException) {
                                        }
                                    }
                                }
                                onDeleteBlock(block.id)
                            },
                            onFocused = { cursorIndex ->
                                onBlockFocusChanged(block.id)
                                onBlockFocused(block.id, cursorIndex)
                            },
                            onBlockFocusChanged = { id ->
                                onBlockFocusChanged(id)
                            },
                            modifier = blockModifier
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
                    onTapBelowBlocks()
                }
        )
    }
}
