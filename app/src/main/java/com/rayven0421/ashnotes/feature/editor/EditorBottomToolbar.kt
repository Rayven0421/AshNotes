package com.rayven0421.ashnotes.feature.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.Block
import com.rayven0421.ashnotes.domain.model.TextBlock
import kotlin.reflect.KClass

@Composable
fun EditorBottomToolbar(
    onInsertBlock: (KClass<out TextBlock>) -> Unit,
    onInsertDivider: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }

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
            border = BorderStroke(AshNotesTheme.spacing.hairline, AshNotesTheme.colors.line),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.widthIn(max = EditorTokens.ContentMaxWidth)
        ) {
            Row(
                modifier = Modifier
                    .height(EditorTokens.ToolbarHeight)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.extraSmall)
            ) {
                Box {
                    ToolbarIconButton(
                        icon = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.insert_menu_button),
                        isActive = isMenuOpen,
                        onClick = { isMenuOpen = true }
                    )

                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false },
                        modifier = Modifier.background(AshNotesTheme.colors.raised)
                    ) {
                        DropdownMenuGroupHeader(text = stringResource(R.string.insert_group_text))
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.insert_paragraph)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.ShortText, contentDescription = null) },
                            onClick = {
                                isMenuOpen = false
                                onInsertBlock(Block.Paragraph::class)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.insert_heading)) },
                            leadingIcon = { Icon(Icons.Outlined.Title, contentDescription = null) },
                            onClick = {
                                isMenuOpen = false
                                onInsertBlock(Block.Heading::class)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.insert_quote)) },
                            leadingIcon = { Icon(Icons.Outlined.FormatQuote, contentDescription = null) },
                            onClick = {
                                isMenuOpen = false
                                onInsertBlock(Block.Quote::class)
                            }
                        )

                        HorizontalDivider(
                            thickness = AshNotesTheme.spacing.hairline,
                            color = AshNotesTheme.colors.line
                        )

                        DropdownMenuGroupHeader(text = stringResource(R.string.insert_group_lists))
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.insert_bullet)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = null) },
                            onClick = {
                                isMenuOpen = false
                                onInsertBlock(Block.BulletItem::class)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.insert_numbered)) },
                            leadingIcon = { Icon(Icons.Outlined.FormatListNumbered, contentDescription = null) },
                            onClick = {
                                isMenuOpen = false
                                onInsertBlock(Block.NumberedItem::class)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.insert_checklist)) },
                            leadingIcon = { Icon(Icons.Outlined.CheckBox, contentDescription = null) },
                            onClick = {
                                isMenuOpen = false
                                onInsertBlock(Block.Checklist::class)
                            }
                        )

                        HorizontalDivider(
                            thickness = AshNotesTheme.spacing.hairline,
                            color = AshNotesTheme.colors.line
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.insert_divider)) },
                            leadingIcon = { Icon(Icons.Outlined.HorizontalRule, contentDescription = null) },
                            onClick = {
                                isMenuOpen = false
                                onInsertDivider()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownMenuGroupHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = AshNotesTheme.colors.textMuted,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun ToolbarIconButton(
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
            .size(EditorTokens.TouchTargetSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = EditorTokens.TouchTargetSize / 2),
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
