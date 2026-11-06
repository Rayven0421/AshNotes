package com.rayven0421.ashnotes.feature.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import com.rayven0421.ashnotes.feature.editor.blocks.ChecklistBlockItem
import com.rayven0421.ashnotes.feature.editor.blocks.HeadingBlockItem
import com.rayven0421.ashnotes.feature.editor.blocks.ParagraphBlockItem

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
    onBlockFocused: (String, Int) -> Unit,
    onTapBelowBlocks: () -> Unit,
    onBlockFocusChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tapInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth()
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
                val itemFocusRequest = if (focusRequest?.blockId == block.id) focusRequest else null

                when (block) {
                    is Block.Paragraph -> {
                        val isOnlyBlockAndEmpty = blocks.size == 1 && block.text.isEmpty()
                        ParagraphBlockItem(
                            block = block,
                            isOnlyBlockAndEmpty = isOnlyBlockAndEmpty,
                            focusRequest = itemFocusRequest,
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
                            onPasteMultiLine = { pastedText, cursorIndex ->
                                onPasteTextInParagraph(block.id, pastedText, cursorIndex)
                            },
                            onFocused = { cursorIndex ->
                                onBlockFocusChanged(block.id)
                                onBlockFocused(block.id, cursorIndex)
                            }
                        )
                    }
                    is Block.Heading -> {
                        HeadingBlockItem(
                            block = block,
                            focusRequest = itemFocusRequest,
                            modifier = blockModifier,
                            onTextChanged = { newText ->
                                onHeadingTextChanged(block.id, newText)
                            },
                            onEnterPressed = { cursorIndex ->
                                onSplitHeading(block.id, cursorIndex)
                            },
                            onBackspaceAtStart = {
                                onBackspaceInParagraph(block.id, 0)
                            },
                            onPasteMultiLine = { pastedText, cursorIndex ->
                                onPasteTextInHeading(block.id, pastedText, cursorIndex)
                            },
                            onFocused = { cursorIndex ->
                                onBlockFocusChanged(block.id)
                                onBlockFocused(block.id, cursorIndex)
                            }
                        )
                    }
                    is Block.Checklist -> {
                        ChecklistBlockItem(
                            block = block,
                            focusRequest = itemFocusRequest,
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
                                onBlockFocusChanged(block.id)
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
                    onTapBelowBlocks()
                }
        )
    }
}
