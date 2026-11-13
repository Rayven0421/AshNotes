package com.rayven0421.ashnotes.feature.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    totalCount: Int,
    onCloseSelection: () -> Unit,
    onSelectAllToggle: () -> Unit,
    onBulkPin: () -> Unit,
    onBulkArchive: () -> Unit,
    onBulkDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isArchivedView: Boolean = false
) {
    val isAllSelected = selectedCount == totalCount && totalCount > 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp), // Match search row height so list doesn't jump
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseSelection) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = pluralStringResource(R.plurals.selection_count, selectedCount, selectedCount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSelectAllToggle) {
                Icon(
                    imageVector = if (isAllSelected) Icons.Outlined.Deselect else Icons.Outlined.SelectAll,
                    contentDescription = stringResource(if (isAllSelected) R.string.action_deselect_all else R.string.action_select_all),
                    tint = AshNotesTheme.colors.textMuted
                )
            }
            IconButton(onClick = onBulkPin) {
                Icon(
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = stringResource(R.string.note_menu_pin),
                    tint = AshNotesTheme.colors.textMuted
                )
            }
            IconButton(onClick = onBulkArchive) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = stringResource(if (isArchivedView) R.string.note_menu_unarchive else R.string.note_menu_archive),
                    tint = AshNotesTheme.colors.textMuted
                )
            }
            IconButton(onClick = onBulkDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = AshNotesTheme.colors.over
                )
            }
        }
    }
}
