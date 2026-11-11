package com.rayven0421.ashnotes.feature.budget.components

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
import androidx.compose.ui.text.input.KeyboardType
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.MoneyUtils
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.AshSmallShape
import com.rayven0421.ashnotes.core.designsystem.dialog.AshDialog

@Composable
fun CreateBudgetDialog(
    onConfirm: (name: String, amountMinor: Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nameText by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val defaultName = stringResource(R.string.budget_default_name)

    val parsedAmount = remember(amountText) {
        MoneyUtils.parseToMinorUnits(amountText)
    }

    val isAmountInvalid = amountText.isNotBlank() && (parsedAmount == null || parsedAmount <= 0L)
    val isFormValid = parsedAmount != null && parsedAmount > 0L

    val handleConfirm = {
        val amount = parsedAmount
        if (amount != null && amount > 0L) {
            val finalName = nameText.trim().ifBlank { defaultName }
            onConfirm(finalName, amount)
        }
    }

    AshDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.dialog_create_budget_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        OutlinedTextField(
            value = nameText,
            onValueChange = { nameText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.field_budget_name_label)) },
            placeholder = { Text(text = stringResource(R.string.field_budget_name_placeholder), color = AshNotesTheme.colors.textMuted) },
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = { Text(text = stringResource(R.string.field_budget_amount_label)) },
            placeholder = { Text(text = stringResource(R.string.field_budget_amount_placeholder), color = AshNotesTheme.colors.textMuted) },
            isError = isAmountInvalid,
            supportingText = if (isAmountInvalid) {
                { Text(text = stringResource(R.string.error_invalid_amount), color = AshNotesTheme.colors.over) }
            } else null,
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
                unfocusedLabelColor = AshNotesTheme.colors.textMuted,
                errorBorderColor = AshNotesTheme.colors.over
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
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
                    text = stringResource(R.string.action_cancel),
                    color = AshNotesTheme.colors.textMuted
                )
            }

            Button(
                onClick = handleConfirm,
                enabled = isFormValid,
                modifier = Modifier.heightIn(min = AshNotesTheme.spacing.huge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AshNotesTheme.colors.ember,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = AshSmallShape
            ) {
                Text(text = stringResource(R.string.action_create))
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
