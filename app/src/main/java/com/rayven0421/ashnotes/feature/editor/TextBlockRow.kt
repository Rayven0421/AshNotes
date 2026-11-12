package com.rayven0421.ashnotes.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.Block
import com.rayven0421.ashnotes.domain.model.FocusRequest
import com.rayven0421.ashnotes.domain.model.TextBlock

@Composable
fun TextBlockRow(
    block: TextBlock,
    modifier: Modifier = Modifier,
    number: Int = 0,
    isOnlyBlockAndEmpty: Boolean = false,
    focusRequest: FocusRequest? = null,
    onTextChanged: (String) -> Unit,
    onEnterPressed: (Int) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onPasteMultiLine: (String, Int) -> Unit,
    onFocused: (Int) -> Unit,
    onToggleChecked: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val textStyle = when (block) {
        is Block.Heading -> MaterialTheme.typography.titleLarge.copy(
            color = MaterialTheme.colorScheme.onBackground
        )
        is Block.Quote -> MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
        )
        is Block.Checklist -> {
            val baseTextStyle = MaterialTheme.typography.bodyLarge
            if (block.isChecked) {
                baseTextStyle.copy(
                    color = AshNotesTheme.colors.textMuted,
                    textDecoration = TextDecoration.LineThrough
                )
            } else {
                baseTextStyle.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        else -> MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    val placeholder = when (block) {
        is Block.Paragraph -> if (isOnlyBlockAndEmpty) stringResource(R.string.editor_paragraph_placeholder) else null
        is Block.Heading -> stringResource(R.string.editor_heading_placeholder)
        else -> null
    }

    val testTag = when (block) {
        is Block.Paragraph -> "paragraph_text"
        is Block.BulletItem -> "bullet_text"
        is Block.NumberedItem -> "numbered_text"
        is Block.Quote -> "quote_text"
        is Block.Checklist -> "checklist_text"
        else -> null
    }

    BlockContainer(
        isFocused = isFocused,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            LeadingSlot(
                block = block,
                number = number,
                textStyle = textStyle,
                onToggleChecked = onToggleChecked
            )

            BlockTextField(
                blockId = block.id,
                blockText = block.text,
                focusRequest = focusRequest,
                textStyle = textStyle,
                onTextChanged = onTextChanged,
                onSplit = onEnterPressed,
                onBackspaceAtStart = onBackspaceAtStart,
                onPaste = onPasteMultiLine,
                onFocused = onFocused,
                placeholder = placeholder,
                onFocusStateChanged = { isFocused = it },
                modifier = Modifier
                    .weight(1f)
                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            )
        }
    }
}

@Composable
private fun LeadingSlot(
    block: TextBlock,
    number: Int,
    textStyle: TextStyle,
    onToggleChecked: (() -> Unit)?
) {
    when (block) {
        is Block.BulletItem -> {
            val density = LocalDensity.current
            val lineHeightDp = with(density) {
                textStyle.lineHeight.takeOrElse { textStyle.fontSize }.toDp()
            }
            val bulletTopPadding = ((lineHeightDp - EditorTokens.BulletSize) / 2).coerceAtLeast(AshNotesTheme.spacing.none)

            Box(
                modifier = Modifier
                    .width(EditorTokens.ListGutterWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = bulletTopPadding)
                        .size(EditorTokens.BulletSize)
                        .clip(CircleShape)
                        .background(AshNotesTheme.colors.textMuted)
                )
            }
        }
        is Block.NumberedItem -> {
            val numberStyle = MaterialTheme.typography.bodyLarge.copy(
                color = AshNotesTheme.colors.textMuted,
                fontFeatureSettings = "tnum"
            )

            Box(
                modifier = Modifier
                    .width(EditorTokens.ListGutterWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = "$number.",
                    style = numberStyle
                )
            }
        }
        is Block.Checklist -> {
            val density = LocalDensity.current
            val lineHeightDp = with(density) {
                textStyle.lineHeight.takeOrElse { textStyle.fontSize }.toDp()
            }
            val boxTopPadding = ((lineHeightDp - EditorTokens.ChecklistBoxSize) / 2).coerceAtLeast(AshNotesTheme.spacing.none)
            val interactionSource = remember { MutableInteractionSource() }
            val contentDesc = stringResource(if (block.isChecked) R.string.checkbox_mark_not_done else R.string.checkbox_mark_done)

            Box(
                modifier = Modifier
                    .width(EditorTokens.ListGutterWidth)
                    .fillMaxHeight()
                    .testTag("checklist_checkbox")
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, radius = EditorTokens.ListGutterWidth / 2),
                        onClick = { onToggleChecked?.invoke() }
                    )
                    .semantics {
                        selected = block.isChecked
                        contentDescription = contentDesc
                    },
                contentAlignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = boxTopPadding)
                        .size(EditorTokens.ChecklistBoxSize)
                        .clip(RoundedCornerShape(EditorTokens.ChecklistCornerRadius))
                        .background(if (block.isChecked) AshNotesTheme.colors.ember else Color.Transparent)
                        .then(
                            if (block.isChecked) Modifier
                            else Modifier.border(AshNotesTheme.spacing.hairline, AshNotesTheme.colors.line, RoundedCornerShape(EditorTokens.ChecklistCornerRadius))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (block.isChecked) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(EditorTokens.ChecklistCheckIconSize)
                        )
                    }
                }
            }
        }
        is Block.Quote -> {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .width(EditorTokens.QuoteBarWidth)
                        .fillMaxHeight()
                        .background(
                            AshNotesTheme.colors.line,
                            RoundedCornerShape(1.dp)
                        )
                )

                Spacer(modifier = Modifier.width(AshNotesTheme.spacing.small))
            }
        }
        else -> {
            Box(modifier = Modifier.width(0.dp))
        }
    }
}
