package com.rayven0421.ashnotes.feature.budget

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.BudgetMeter

import com.rayven0421.ashnotes.core.designsystem.ScreenHeader

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<BudgetContent> = UiState.Empty,
    onCreateBudgetClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = AshNotesTheme.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
    ) {
        ScreenHeader(title = stringResource(R.string.budget_screen_title))

            when (uiState) {
                is UiState.Empty, is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
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
                            OutlinedButton(onClick = onCreateBudgetClick) {
                                Text(text = stringResource(R.string.budget_empty_action))
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
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
                        TransactionGroupList(groups = content.transactionGroups)
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
            isOverBudget -> stringResource(R.string.budget_over_label, formatMoney(content.spentMinor - content.totalMinor))
            else -> formatMoney(remainingMinor)
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

        // Reused Budget Meter
        BudgetMeter(
            spentMinor = content.spentMinor,
            totalMinor = content.totalMinor,
            paceFraction = content.paceFraction
        )

        val summaryText = when {
            isZeroBudget -> stringResource(R.string.budget_summary, formatMoney(0L), formatMoney(0L), 0)
            isOverBudget -> stringResource(
                R.string.budget_summary_over,
                formatMoney(content.spentMinor),
                formatMoney(content.spentMinor - content.totalMinor),
                content.daysLeft
            )
            else -> stringResource(
                R.string.budget_summary,
                formatMoney(content.spentMinor),
                formatMoney(content.totalMinor),
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
                        text = formatMoney(category.spentMinor),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(AshNotesTheme.colors.line, RoundedCornerShape(3.dp))
                ) {
                    val clampedProportion = category.proportion.coerceIn(0f, 1f)
                    if (clampedProportion > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(clampedProportion)
                                .height(6.dp)
                                .background(AshNotesTheme.colors.ember, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionGroupList(groups: List<TransactionGroup>) {
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
                            .padding(vertical = AshNotesTheme.spacing.small),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.description,
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
                            text = formatMoney(item.amountMinor),
                            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.End
                        )
                    }
                    if (index < group.transactions.size - 1) {
                        HorizontalDivider(
                            color = AshNotesTheme.colors.line,
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

private fun formatMoney(amountMinor: Long): String {
    val absAmount = Math.abs(amountMinor)
    val major = absAmount / 100
    val minor = absAmount % 100
    val formatted = String.format(java.util.Locale.getDefault(), "₱%,d.%02d", major, minor)
    return if (amountMinor < 0) "-$formatted" else formatted
}

// Sample* data used ONLY by @Preview functions per instructions
private object SampleBudgetData {
    val successState = BudgetContent(
        title = "Monthly Budget",
        totalMinor = 500000L,
        spentMinor = 325000L,
        daysLeft = 12,
        paceFraction = 0.6f,
        categories = listOf(
            CategorySpendingItem("c1", "Groceries & Food", 145000L, 0.446f),
            CategorySpendingItem("c2", "Utilities & Internet", 98000L, 0.301f),
            CategorySpendingItem("c3", "Dining & Coffee", 52000L, 0.160f),
            CategorySpendingItem("c4", "Transport", 30000L, 0.093f)
        ),
        transactionGroups = listOf(
            TransactionGroup(
                dateLabel = "Today",
                transactions = listOf(
                    TransactionItem("t1", "Dining & Coffee", "Artisan Coffee Roasters", 45000L),
                    TransactionItem("t2", "Groceries & Food", "Organic Supermarket", 125000L)
                )
            ),
            TransactionGroup(
                dateLabel = "Yesterday",
                transactions = listOf(
                    TransactionItem("t3", "Utilities & Internet", "Fiber Internet Bill", 280000L)
                )
            )
        )
    )

    val overBudgetState = BudgetContent(
        title = "Monthly Budget",
        totalMinor = 500000L,
        spentMinor = 532000L,
        daysLeft = 5,
        paceFraction = 0.85f,
        categories = listOf(
            CategorySpendingItem("c1", "Groceries & Food", 245000L, 0.460f),
            CategorySpendingItem("c2", "Utilities & Internet", 198000L, 0.372f),
            CategorySpendingItem("c3", "Dining & Coffee", 89000L, 0.168f)
        ),
        transactionGroups = listOf(
            TransactionGroup(
                dateLabel = "Today",
                transactions = listOf(
                    TransactionItem("t1", "Groceries & Food", "Bulk Wholesale Grocery", 320000L)
                )
            )
        )
    )

    val zeroBudgetState = BudgetContent(
        title = "Unset Budget",
        totalMinor = 0L,
        spentMinor = 0L,
        daysLeft = 0,
        paceFraction = 0f,
        categories = emptyList(),
        transactionGroups = emptyList()
    )
}

@Preview(name = "Budget - Success", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun BudgetScreenSuccessPreview() {
    AshNotesTheme {
        BudgetScreen(uiState = UiState.Success(SampleBudgetData.successState))
    }
}

@Preview(name = "Budget - Empty", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun BudgetScreenEmptyPreview() {
    AshNotesTheme {
        BudgetScreen(uiState = UiState.Empty)
    }
}

@Preview(name = "Budget - Over Budget", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun BudgetScreenOverBudgetPreview() {
    AshNotesTheme {
        BudgetScreen(uiState = UiState.Success(SampleBudgetData.overBudgetState))
    }
}

@Preview(name = "Budget - Zero Budget", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun BudgetScreenZeroBudgetPreview() {
    AshNotesTheme {
        BudgetScreen(uiState = UiState.Success(SampleBudgetData.zeroBudgetState))
    }
}
