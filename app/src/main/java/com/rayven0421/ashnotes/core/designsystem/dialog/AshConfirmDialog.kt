package com.rayven0421.ashnotes.core.designsystem.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.AshSmallShape

@Composable
fun AshConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    cancelLabel: String = stringResource(R.string.action_cancel),
    isDestructive: Boolean = false
) {
    AshDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = AshNotesTheme.colors.textMuted
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cancelLabel.isNotEmpty()) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.heightIn(min = AshNotesTheme.spacing.huge)
                ) {
                    Text(
                        text = cancelLabel,
                        color = AshNotesTheme.colors.textMuted
                    )
                }
            }

            val confirmColor = if (isDestructive) AshNotesTheme.colors.over else AshNotesTheme.colors.ember

            Button(
                onClick = onConfirm,
                modifier = Modifier.heightIn(min = AshNotesTheme.spacing.huge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = AshSmallShape
            ) {
                Text(text = confirmLabel)
            }
        }
    }
}
