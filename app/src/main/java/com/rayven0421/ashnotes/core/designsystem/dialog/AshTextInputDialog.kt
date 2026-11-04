package com.rayven0421.ashnotes.core.designsystem.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.AshSmallShape

@Composable
fun AshTextInputDialog(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    initialValue: String = "",
    placeholder: String? = null,
    label: String? = null,
    maxLength: Int? = null,
    allowBlank: Boolean = false,
    blankFallbackValue: String = stringResource(R.string.note_title_untitled),
    confirmLabel: String = stringResource(R.string.action_save),
    cancelLabel: String = stringResource(R.string.action_cancel)
) {
    var text by rememberSaveable { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }

    val handleConfirm = {
        val trimmed = text.trim()
        val finalResult = when {
            trimmed.isNotBlank() -> trimmed
            allowBlank -> trimmed
            else -> blankFallbackValue
        }
        onConfirm(finalResult)
    }

    AshDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (!supportingText.isNullOrEmpty()) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = AshNotesTheme.colors.textMuted
            )
        }

        OutlinedTextField(
            value = text,
            onValueChange = { newValue ->
                if (maxLength == null || newValue.length <= maxLength) {
                    text = newValue
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = label?.let { { Text(text = it) } },
            placeholder = placeholder?.let { { Text(text = it, color = AshNotesTheme.colors.textMuted) } },
            singleLine = true,
            shape = AshSmallShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AshNotesTheme.colors.ember,
                unfocusedBorderColor = AshNotesTheme.colors.line,
                focusedContainerColor = AshNotesTheme.colors.raised,
                unfocusedContainerColor = AshNotesTheme.colors.raised,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = AshNotesTheme.colors.ember,
                unfocusedLabelColor = AshNotesTheme.colors.textMuted
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { handleConfirm() })
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
                    text = cancelLabel,
                    color = AshNotesTheme.colors.textMuted
                )
            }

            Button(
                onClick = handleConfirm,
                modifier = Modifier.heightIn(min = AshNotesTheme.spacing.huge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AshNotesTheme.colors.ember,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = AshSmallShape
            ) {
                Text(text = confirmLabel)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
