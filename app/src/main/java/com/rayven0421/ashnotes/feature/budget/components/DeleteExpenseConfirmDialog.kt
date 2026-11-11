package com.rayven0421.ashnotes.feature.budget.components

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
import com.rayven0421.ashnotes.core.designsystem.dialog.AshDialog

@Composable
fun DeleteExpenseConfirmDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AshDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.dialog_delete_expense_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.dialog_delete_expense_message),
            style = MaterialTheme.typography.bodyMedium,
            color = AshNotesTheme.colors.textMuted
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = AshNotesTheme.spacing.huge)
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = AshNotesTheme.colors.textMuted
                )
            }

            Button(
                onClick = onConfirmDelete,
                modifier = Modifier.heightIn(min = AshNotesTheme.spacing.huge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AshNotesTheme.colors.over,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = AshSmallShape
            ) {
                Text(text = stringResource(R.string.action_delete))
            }
        }
    }
}
