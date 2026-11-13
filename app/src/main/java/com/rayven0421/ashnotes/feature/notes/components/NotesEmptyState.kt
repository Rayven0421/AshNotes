package com.rayven0421.ashnotes.feature.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

@Composable
fun NotesEmptyState(
    onNewNoteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isArchivedView: Boolean = false
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
        ) {
            Text(
                text = stringResource(if (isArchivedView) R.string.notes_empty_archived else R.string.notes_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = AshNotesTheme.colors.textMuted
            )
            if (!isArchivedView) {
                OutlinedButton(onClick = onNewNoteClick) {
                    Text(text = stringResource(R.string.notes_new_note))
                }
            }
        }
    }
}
