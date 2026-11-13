package com.rayven0421.ashnotes.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.MoneyUtils
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.BudgetMeter
import com.rayven0421.ashnotes.core.designsystem.TabScreenLayout
import com.rayven0421.ashnotes.core.designsystem.dialog.AshTextInputDialog
import java.util.Calendar

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<HomeContent> = UiState.Empty,
    onNavigateToBudget: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    onNewNoteClick: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showNewNoteDialog by rememberSaveable { mutableStateOf(false) }

    val greetingResId = if (uiState is UiState.Success) {
        uiState.data.greetingResId
    } else {
        getGreetingByTimeOfDayResId()
    }

    TabScreenLayout(
        title = stringResource(greetingResId),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = AshNotesTheme.spacing.listBottomPadding),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
        ) {
            if (uiState is UiState.Success) {
                Text(
                    text = stringResource(R.string.home_active_workspace, uiState.data.activeWorkspaceName),
                    style = MaterialTheme.typography.labelMedium,
                    color = AshNotesTheme.colors.textMuted
                )
            }

            HomeBudgetSection(
                uiState = uiState,
                onNavigateToBudget = onNavigateToBudget
            )

            HomeNotesSection(
                uiState = uiState,
                onNoteClick = onNoteClick
            )

            HomeExpensesSection(
                uiState = uiState,
                onNavigateToBudget = onNavigateToBudget
            )

            HomeQuickActionsRow(
                onAddExpenseClick = onAddExpenseClick,
                onNewNoteClick = { showNewNoteDialog = true }
            )

            val footerText = when (uiState) {
                is UiState.Success -> {
                    val email = uiState.data.userEmail
                    if (email != null) {
                        stringResource(R.string.home_footer_signed_in, email)
                    } else {
                        stringResource(R.string.home_footer_local_only)
                    }
                }
                else -> stringResource(R.string.home_footer_local_only)
            }
            Text(
                text = footerText,
                style = MaterialTheme.typography.labelMedium,
                color = AshNotesTheme.colors.textMuted
            )
        }
    }

    if (showNewNoteDialog) {
        AshTextInputDialog(
            title = stringResource(R.string.dialog_new_note_title),
            label = stringResource(R.string.field_note_title_label),
            confirmLabel = stringResource(R.string.action_create),
            blankFallbackValue = stringResource(R.string.note_title_untitled),
            onConfirm = { title ->
                onNewNoteClick(title)
                showNewNoteDialog = false
            },
            onDismiss = { showNewNoteDialog = false }
        )
    }
}

private fun getGreetingByTimeOfDayResId(): Int {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..17 -> R.string.home_greeting_afternoon
        else -> R.string.home_greeting_evening
    }
}

@Composable
private fun HomeBudgetSection(
    uiState: UiState<HomeContent>,
    onNavigateToBudget: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToBudget)
            .padding(vertical = AshNotesTheme.spacing.small),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
    ) {
        Text(
            text = stringResource(R.string.home_budget_label),
            style = MaterialTheme.typography.labelMedium,
            color = AshNotesTheme.colors.textMuted
        )

        when (uiState) {
            is UiState.Empty, is UiState.Loading -> {
                Text(
                    text = stringResource(R.string.home_budget_empty_action),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AshNotesTheme.colors.ember
                )
            }
            is UiState.Success -> {
                val content = uiState.data
                if (!content.hasBudget) {
                    Text(
                        text = stringResource(R.string.home_budget_empty_action),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AshNotesTheme.colors.ember
                    )
                } else {
                    val spentStr = MoneyUtils.formatMoney(content.spentMinor)
                    val totalStr = MoneyUtils.formatMoney(content.totalMinor)
                    val summaryText = if (content.spentMinor > content.totalMinor) {
                        val overMinor = content.spentMinor - content.totalMinor
                        stringResource(R.string.home_budget_over, MoneyUtils.formatMoney(overMinor))
                    } else {
                        stringResource(R.string.home_budget_spent, spentStr, totalStr)
                    }

                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (content.spentMinor > content.totalMinor) AshNotesTheme.colors.over else MaterialTheme.colorScheme.onBackground
                    )

                    BudgetMeter(
                        spentMinor = content.spentMinor,
                        totalMinor = content.totalMinor,
                        paceFraction = content.paceFraction
                    )
                }
            }
            is UiState.Error -> {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AshNotesTheme.colors.over
                )
            }
        }
    }
}

@Composable
private fun HomeNotesSection(
    uiState: UiState<HomeContent>,
    onNoteClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
    ) {
        Text(
            text = stringResource(R.string.home_notes_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        when (uiState) {
            is UiState.Empty, is UiState.Loading -> {
                Text(
                    text = stringResource(R.string.home_notes_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshNotesTheme.colors.textMuted
                )
            }
            is UiState.Success -> {
                val notes = uiState.data.notes
                if (notes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.home_notes_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshNotesTheme.colors.textMuted
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.none)) {
                        notes.forEachIndexed { index, note ->
                            HomeNoteRow(note = note, onClick = { onNoteClick(note.id) })
                            if (index < notes.size - 1) {
                                HorizontalDivider(
                                    color = AshNotesTheme.colors.line,
                                    thickness = AshNotesTheme.spacing.hairline
                                )
                            }
                        }
                    }
                }
            }
            is UiState.Error -> {
                Text(text = uiState.message, color = AshNotesTheme.colors.over)
            }
        }
    }
}

@Composable
private fun HomeNoteRow(
    note: HomeNoteItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AshNotesTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.extraSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.title.ifEmpty { stringResource(R.string.note_title_untitled) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(AshNotesTheme.spacing.default))
            Text(
                text = note.updatedTime,
                style = MaterialTheme.typography.labelSmall,
                color = AshNotesTheme.colors.textMuted,
                maxLines = 1
            )
        }
        if (note.preview.isNotEmpty()) {
            Text(
                text = note.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = AshNotesTheme.colors.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeExpensesSection(
    uiState: UiState<HomeContent>,
    onNavigateToBudget: () -> Unit
) {
    if (uiState !is UiState.Success) return
    val expenses = uiState.data.recentExpenses
    if (expenses.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_expenses_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.home_expenses_see_all),
                style = MaterialTheme.typography.labelLarge,
                color = AshNotesTheme.colors.ember,
                modifier = Modifier
                    .clickable(onClick = onNavigateToBudget)
                    .padding(vertical = AshNotesTheme.spacing.extraSmall)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.none)) {
            expenses.forEachIndexed { index, expense ->
                HomeExpenseRow(expense = expense)
                if (index < expenses.size - 1) {
                    HorizontalDivider(
                        color = AshNotesTheme.colors.line,
                        thickness = AshNotesTheme.spacing.hairline
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeExpenseRow(
    expense: HomeExpenseItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AshNotesTheme.spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = AshNotesTheme.spacing.default),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.extraSmall)
        ) {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = expense.dateText,
                style = MaterialTheme.typography.labelSmall,
                color = AshNotesTheme.colors.textMuted,
                maxLines = 1
            )
        }
        Text(
            text = MoneyUtils.formatMoney(expense.amountMinor),
            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun HomeQuickActionsRow(
    onAddExpenseClick: () -> Unit,
    onNewNoteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.default)
    ) {
        OutlinedButton(
            onClick = onNewNoteClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.home_action_new_note))
        }
        OutlinedButton(
            onClick = onAddExpenseClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.home_action_add_expense))
        }
    }
}
