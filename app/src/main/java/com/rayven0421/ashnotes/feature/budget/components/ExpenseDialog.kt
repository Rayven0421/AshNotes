package com.rayven0421.ashnotes.feature.budget.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.MoneyUtils
import com.rayven0421.ashnotes.core.common.formatExpenseDisplayDate
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.AshSmallShape
import com.rayven0421.ashnotes.core.designsystem.dialog.AshDialog
import com.rayven0421.ashnotes.domain.model.Category
import com.rayven0421.ashnotes.domain.model.Expense
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDialog(
    categories: List<Category>,
    onConfirm: (categoryId: String, amountMinor: Long, date: Long, note: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    expenseToEdit: Expense? = null,
    onDeleteExpense: (() -> Unit)? = null
) {
    val initialAmountText = remember(expenseToEdit) {
        if (expenseToEdit != null) {
            val formatted = MoneyUtils.formatMoney(expenseToEdit.amountMinor, "")
            formatted.trim()
        } else ""
    }

    var amountText by rememberSaveable { mutableStateOf(initialAmountText) }
    var selectedCategoryId by rememberSaveable {
        mutableStateOf(expenseToEdit?.categoryId ?: categories.firstOrNull()?.id ?: "")
    }
    var selectedDateMs by rememberSaveable {
        mutableLongStateOf(expenseToEdit?.date ?: System.currentTimeMillis())
    }
    var noteText by rememberSaveable(expenseToEdit) { mutableStateOf(expenseToEdit?.description ?: "") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (selectedCategoryId.isBlank() && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    val focusRequester = remember { FocusRequester() }

    val parsedAmount = remember(amountText) {
        MoneyUtils.parseToMinorUnits(amountText)
    }

    val isAmountInvalid = amountText.isNotBlank() && (parsedAmount == null || parsedAmount <= 0L)
    val isFormValid = parsedAmount != null && parsedAmount > 0L && selectedCategoryId.isNotBlank()

    val handleConfirm = {
        val amount = parsedAmount
        if (amount != null && amount > 0L && selectedCategoryId.isNotBlank()) {
            onConfirm(selectedCategoryId, amount, selectedDateMs, noteText.trim())
        }
    }

    val context = LocalContext.current
    val openDatePicker = {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMs }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedDateMs = newCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
        ?: categories.firstOrNull()

    AshDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    if (expenseToEdit == null) R.string.dialog_add_expense_title
                    else R.string.dialog_edit_expense_title
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (expenseToEdit != null && onDeleteExpense != null) {
                IconButton(onClick = onDeleteExpense) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.dialog_delete_expense_title),
                        tint = AshNotesTheme.colors.over
                    )
                }
            }
        }

        // 1. Amount First (large font/display style, numeric keyboard)
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = { Text(text = stringResource(R.string.field_expense_amount_label)) },
            placeholder = { Text(text = stringResource(R.string.field_budget_amount_placeholder), color = AshNotesTheme.colors.textMuted) },
            isError = isAmountInvalid,
            supportingText = if (isAmountInvalid) {
                { Text(text = stringResource(R.string.error_invalid_expense_amount), color = AshNotesTheme.colors.over) }
            } else null,
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
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
                imeAction = ImeAction.Next
            )
        )

        // 2. Category (dropdown of seeded categories)
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                label = { Text(text = stringResource(R.string.field_expense_category_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
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
                )
            )

            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = category.name,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            selectedCategoryId = category.id
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        // 3. Date (default today, date picker dialog)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formatExpenseDisplayDate(selectedDateMs),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDatePicker() },
                enabled = false, // Disabled for direct typing, handles clicks via Box/clickable
                label = { Text(text = stringResource(R.string.field_expense_date_label)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.field_expense_date_label),
                        tint = AshNotesTheme.colors.textMuted
                    )
                },
                singleLine = true,
                shape = AshSmallShape,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = AshNotesTheme.colors.line,
                    disabledContainerColor = AshNotesTheme.colors.raised,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledLabelColor = AshNotesTheme.colors.textMuted,
                    disabledTrailingIconColor = AshNotesTheme.colors.textMuted
                )
            )
            // Overlay transparent clickable box to reliably catch click events
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { openDatePicker() }
            )
        }

        // 4. Note
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.field_expense_note_label)) },
            placeholder = { Text(text = stringResource(R.string.field_expense_note_placeholder), color = AshNotesTheme.colors.textMuted) },
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
                    text = stringResource(R.string.action_cancel),
                    color = AshNotesTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

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
                Text(
                    text = stringResource(
                        if (expenseToEdit == null) R.string.action_create
                        else R.string.action_save
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
