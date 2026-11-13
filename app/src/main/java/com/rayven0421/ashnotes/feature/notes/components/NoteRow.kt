package com.rayven0421.ashnotes.feature.notes.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.AshSmallShape
import com.rayven0421.ashnotes.feature.notes.NoteItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteRow(
    note: NoteItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleArchive: (String) -> Unit,
    onRenameRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val rowBackground = if (isSelected) AshNotesTheme.colors.emberSoft else MaterialTheme.colorScheme.background

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { selected = isSelected }
            .background(color = rowBackground, shape = AshSmallShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(
                horizontal = AshNotesTheme.spacing.small,
                vertical = AshNotesTheme.spacing.medium
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Title, preview, and pin indicator
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = AshNotesTheme.spacing.default),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.extraSmall)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = stringResource(R.string.note_menu_pin),
                        tint = AshNotesTheme.colors.ember,
                        modifier = Modifier.size(AshNotesTheme.spacing.default)
                    )
                }
                Text(
                    text = note.title.ifEmpty { stringResource(R.string.note_title_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            if (note.preview.isNotEmpty()) {
                Text(
                    text = note.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshNotesTheme.colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right side: Date in fixed spot + 3-dot menu or check circle (no layout shift)
        Row(
            horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.updatedTime,
                style = MaterialTheme.typography.labelSmall,
                color = AshNotesTheme.colors.textMuted,
                maxLines = 1
            )

            if (isSelectionMode) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.action_selected),
                        tint = if (isSelected) AshNotesTheme.colors.ember else AshNotesTheme.colors.textMuted.copy(alpha = AshNotesTheme.colors.disabledAlpha),
                        modifier = Modifier.size(AshNotesTheme.spacing.large)
                    )
                }
            } else {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.note_menu_more),
                            tint = AshNotesTheme.colors.textMuted
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.note_menu_rename)) },
                            onClick = {
                                showMenu = false
                                onRenameRequest()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        if (note.isPinned) R.string.note_menu_unpin else R.string.note_menu_pin
                                    )
                                )
                            },
                            onClick = {
                                showMenu = false
                                onTogglePin(note.id)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        if (note.isArchived) R.string.note_menu_unarchive else R.string.note_menu_archive
                                    )
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleArchive(note.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.note_menu_delete), color = AshNotesTheme.colors.over) },
                            onClick = {
                                showMenu = false
                                onDeleteRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}
