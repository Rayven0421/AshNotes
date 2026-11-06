package com.rayven0421.ashnotes.feature.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

@Composable
fun EditorHeader(
    saveStatus: SaveStatus,
    errorMessage: String?,
    canUndo: Boolean,
    canRedo: Boolean,
    isKeyboardOpen: Boolean,
    onBackClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = EditorTokens.ContentMaxWidth)
            .height(EditorTokens.HeaderHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(EditorTokens.TouchTargetSize)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AshNotesTheme.colors.raised,
                border = BorderStroke(AshNotesTheme.spacing.hairline, AshNotesTheme.colors.line),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AshNotesTheme.spacing.extraSmall, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onUndoClick,
                        enabled = canUndo,
                        modifier = Modifier.size(EditorTokens.ActionButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Undo,
                            contentDescription = stringResource(R.string.action_undo),
                            tint = if (canUndo) MaterialTheme.colorScheme.onBackground else AshNotesTheme.colors.textMuted.copy(alpha = EditorTokens.DisabledAlpha)
                        )
                    }

                    IconButton(
                        onClick = onRedoClick,
                        enabled = canRedo,
                        modifier = Modifier.size(EditorTokens.ActionButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Redo,
                            contentDescription = stringResource(R.string.action_redo),
                            tint = if (canRedo) MaterialTheme.colorScheme.onBackground else AshNotesTheme.colors.textMuted.copy(alpha = EditorTokens.DisabledAlpha)
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
                        modifier = Modifier.padding(end = 10.dp, start = AshNotesTheme.spacing.extraSmall)
                    )
                }
            }

            if (isKeyboardOpen) {
                Box(
                    modifier = Modifier
                        .size(EditorTokens.ActionButtonSize)
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
