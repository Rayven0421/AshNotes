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
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.BudgetMeter
import com.rayven0421.ashnotes.core.designsystem.ScreenHeader

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<BudgetContent> = UiState.Empty,
    // TODO(phase-4): Wire "Create budget" creation flow when Budget entity exists in Phase 4
    onCreateBudgetClick: () -> Unit = {},
    // TODO(phase-4): Wire "Add expense" creation flow when Expense entity exists in Phase 4
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
                text = formatMoney(category.spentMinor),
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
            TransactionGroupBlock(group = group)
        }
    }
}

@Composable
private fun TransactionGroupBlock(group: TransactionGroup) {
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
                    thickness = AshNotesTheme.spacing.hairline
                )
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
