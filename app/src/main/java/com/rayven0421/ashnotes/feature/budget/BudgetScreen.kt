package com.rayven0421.ashnotes.feature.budget

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.MoneyUtils
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.AshSmallShape
import com.rayven0421.ashnotes.core.designsystem.BudgetMeter
import com.rayven0421.ashnotes.core.designsystem.ScreenHeader
import com.rayven0421.ashnotes.domain.model.Category
import com.rayven0421.ashnotes.domain.model.Expense
import com.rayven0421.ashnotes.feature.budget.components.CreateBudgetDialog
import com.rayven0421.ashnotes.feature.budget.components.DeleteExpenseConfirmDialog
import com.rayven0421.ashnotes.feature.budget.components.ExpenseDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<BudgetContent> = UiState.Empty,
    showCreateDialog: Boolean = false,
    showExpenseDialog: Boolean = false,
    editingExpense: Expense? = null,
    showDeleteConfirmDialog: Expense? = null,
    categories: List<Category> = emptyList(),
    uiEvent: Flow<BudgetUiEvent> = emptyFlow(),
    onCreateBudgetClick: () -> Unit = {},
    onDismissCreateDialog: () -> Unit = {},
    onConfirmCreateBudget: (name: String, amountMinor: Long) -> Unit = { _, _ -> },
    onAddExpenseClick: () -> Unit = {},
    onEditExpenseClick: (Expense) -> Unit = {},
    onDismissExpenseDialog: () -> Unit = {},
    onSaveExpense: (categoryId: String, amountMinor: Long, date: Long, note: String) -> Unit = { _, _, _, _ -> },
    onRequestDeleteExpense: (Expense) -> Unit = {},
    onDismissDeleteConfirmDialog: () -> Unit = {},
    onConfirmDeleteExpense: () -> Unit = {},
    onUndoDeleteExpense: (Expense) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiEvent) {
        uiEvent.collect { event ->
            when (event) {
                is BudgetUiEvent.ShowExpenseDeletedSnackbar -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_expense_deleted),
                        Toast.LENGTH_SHORT
                    ).show()

                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.snackbar_expense_deleted),
                        actionLabel = context.getString(R.string.action_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onUndoDeleteExpense(event.expense)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateBudgetDialog(
            onConfirm = { name, amountMinor ->
                onConfirmCreateBudget(name, amountMinor)
            },
            onDismiss = onDismissCreateDialog
        )
    }

    if (showExpenseDialog) {
        ExpenseDialog(
            categories = categories,
            expenseToEdit = editingExpense,
            onConfirm = onSaveExpense,
            onDismiss = onDismissExpenseDialog,
            onDeleteExpense = {
                editingExpense?.let { onRequestDeleteExpense(it) }
            }
        )
    }

    if (showDeleteConfirmDialog != null) {
        DeleteExpenseConfirmDialog(
            onConfirmDelete = onConfirmDeleteExpense,
            onDismiss = onDismissDeleteConfirmDialog
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = AshNotesTheme.spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScreenHeader(title = stringResource(R.string.budget_screen_title))

                if (uiState is UiState.Success) {
                    OutlinedButton(
                        onClick = onAddExpenseClick,
                        shape = AshSmallShape
                    ) {
                        Text(text = stringResource(R.string.budget_action_add_expense))
                    }
                }
            }

            when (uiState) {
                is UiState.Empty, is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AshNotesTheme.spacing.emptyStateHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
                        ) {
                            Text(
                                text = stringResource(R.string.budget_empty_message),
                                style = MaterialTheme.typography.bodyLarge,
                                color = AshNotesTheme.colors.textMuted
                            )
                            OutlinedButton(
                                onClick = onCreateBudgetClick,
                                shape = AshSmallShape
                            ) {
                                Text(text = stringResource(R.string.budget_empty_action))
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AshNotesTheme.spacing.errorStateHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = AshNotesTheme.colors.over
                        )
                    }
                }
                is UiState.Success -> {
                    val content = uiState.data
                    BudgetHero(content = content)

                    if (content.categories.isNotEmpty()) {
                        CategorySpendingList(categories = content.categories)
                    }

                    if (content.transactionGroups.isNotEmpty()) {
                        TransactionGroupList(
                            groups = content.transactionGroups,
                            onTransactionClick = onEditExpenseClick
                        )
                    } else {
                        EmptyTransactionsBlock(onAddExpenseClick = onAddExpenseClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionsBlock(onAddExpenseClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.budget_transactions_section),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AshNotesTheme.spacing.medium),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.medium)
            ) {
                Text(
                    text = stringResource(R.string.budget_empty_transactions),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AshNotesTheme.colors.textMuted
                )
                OutlinedButton(
                    onClick = onAddExpenseClick,
                    shape = AshSmallShape
                ) {
                    Text(text = stringResource(R.string.budget_action_add_expense))
                }
            }
        }
    }
}

@Composable
private fun BudgetHero(content: BudgetContent) {
    val isOverBudget = content.totalMinor > 0 && content.spentMinor > content.totalMinor
    val isZeroBudget = content.totalMinor == 0L
    val remainingMinor = content.totalMinor - content.spentMinor

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
    ) {
        Text(
            text = stringResource(R.string.budget_remaining_label),
            style = MaterialTheme.typography.labelMedium,
            color = AshNotesTheme.colors.textMuted
        )

        val headlineText = when {
            isZeroBudget -> stringResource(R.string.budget_zero_label)
            isOverBudget -> stringResource(R.string.budget_over_label, MoneyUtils.formatMoney(content.spentMinor - content.totalMinor))
            else -> MoneyUtils.formatMoney(remainingMinor)
        }

        val headlineColor = when {
            isOverBudget -> AshNotesTheme.colors.over
            else -> MaterialTheme.colorScheme.onBackground
        }

        Text(
            text = headlineText,
            style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
            color = headlineColor
        )

        BudgetMeter(
            spentMinor = content.spentMinor,
            totalMinor = content.totalMinor,
            paceFraction = content.paceFraction
        )

        val summaryText = when {
            isZeroBudget -> stringResource(
                R.string.budget_summary,
                MoneyUtils.formatMoney(0L),
                MoneyUtils.formatMoney(0L),
                content.daysLeft
            )
            else -> stringResource(
                R.string.budget_summary,
                MoneyUtils.formatMoney(content.spentMinor),
                MoneyUtils.formatMoney(content.totalMinor),
                content.daysLeft
            )
        }

        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            color = AshNotesTheme.colors.textMuted
        )
    }
}

@Composable
private fun CategorySpendingList(categories: List<CategorySpendingItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.budget_category_section),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        categories.forEach { category ->
            CategoryRow(category = category)
        }
    }
}

@Composable
private fun CategoryRow(category: CategorySpendingItem) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.extraSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = MoneyUtils.formatMoney(category.spentMinor),
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AshNotesTheme.spacing.meterBarHeight)
                .background(
                    AshNotesTheme.colors.line,
                    RoundedCornerShape(AshNotesTheme.spacing.meterBarRadius)
                )
        ) {
            val clampedProportion = category.proportion.coerceIn(0f, 1f)
            if (clampedProportion > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clampedProportion)
                        .height(AshNotesTheme.spacing.meterBarHeight)
                        .background(
                            AshNotesTheme.colors.ember,
                            RoundedCornerShape(AshNotesTheme.spacing.meterBarRadius)
                        )
                )
            }
        }
    }
}

@Composable
private fun TransactionGroupList(
    groups: List<TransactionGroup>,
    onTransactionClick: (Expense) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.budget_transactions_section),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        groups.forEach { group ->
            TransactionGroupBlock(group = group, onTransactionClick = onTransactionClick)
        }
    }
}

@Composable
private fun TransactionGroupBlock(
    group: TransactionGroup,
    onTransactionClick: (Expense) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
    ) {
        Text(
            text = group.dateLabel,
            style = MaterialTheme.typography.titleMedium,
            color = AshNotesTheme.colors.textMuted
        )

        group.transactions.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTransactionClick(item.expense) }
                    .padding(vertical = AshNotesTheme.spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.description.ifBlank { item.categoryName },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = item.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = AshNotesTheme.colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.width(AshNotesTheme.spacing.default))
                Text(
                    text = MoneyUtils.formatMoney(item.amountMinor),
                    style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End
                )
            }
            if (index < group.transactions.size - 1) {
                HorizontalDivider(
                    color = AshNotesTheme.colors.line,
                    thickness = AshNotesTheme.spacing.hairline
                )
            }
        }
    }
}
