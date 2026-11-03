package com.rayven0421.ashnotes.feature.home

import androidx.annotation.StringRes
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.BudgetMeter
import com.rayven0421.ashnotes.core.designsystem.ScreenHeader

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<HomeContent> = UiState.Empty,
    onNavigateToNotes: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNoteClick: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = AshNotesTheme.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
    ) {
        val greeting = when (uiState) {
            is UiState.Success -> uiState.data.greeting
            else -> stringResource(getGreetingByTimeOfDayResId())
        }
        ScreenHeader(title = greeting)

        HomeBudgetSection(
            uiState = uiState,
            onNavigateToBudget = onNavigateToBudget
        )

        HomeNotesSection(
            uiState = uiState,
            onNoteClick = onNoteClick
        )

        HomeActivitySection(uiState = uiState)

        HomeQuickActionsRow(
            onNavigateToNotes = onNavigateToNotes,
            onNavigateToBudget = onNavigateToBudget
        )

        val syncStatus = when (uiState) {
            is UiState.Success -> uiState.data.syncStatus
            else -> stringResource(R.string.home_sync_local_only)
        }
        Text(
            text = syncStatus,
            style = MaterialTheme.typography.labelMedium,
            color = AshNotesTheme.colors.textMuted
        )
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
                    val spentStr = formatMoney(content.spentMinor)
                    val totalStr = formatMoney(content.totalMinor)
                    val summaryText = if (content.spentMinor > content.totalMinor) {
                        val overMinor = content.spentMinor - content.totalMinor
                        stringResource(R.string.home_budget_over, formatMoney(overMinor))
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
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = note.updatedTime,
                style = MaterialTheme.typography.labelSmall,
                color = AshNotesTheme.colors.textMuted
            )
        }
        Text(
            text = note.preview,
            style = MaterialTheme.typography.bodyMedium,
            color = AshNotesTheme.colors.textMuted,
            maxLines = 2
        )
    }
}

@Composable
private fun HomeActivitySection(uiState: UiState<HomeContent>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
    ) {
        Text(
            text = stringResource(R.string.home_activity_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        val activities = when (uiState) {
            is UiState.Success -> uiState.data.activity
            else -> emptyList()
        }

        if (activities.isEmpty()) {
            Text(
                text = stringResource(R.string.home_activity_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = AshNotesTheme.colors.textMuted
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)) {
                activities.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AshNotesTheme.spacing.extraSmall),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(AshNotesTheme.spacing.default))
                        Text(
                            text = item.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = AshNotesTheme.colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeQuickActionsRow(
    onNavigateToNotes: () -> Unit,
    onNavigateToBudget: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.default)
    ) {
        OutlinedButton(
            onClick = onNavigateToNotes,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.home_action_new_note))
        }
        OutlinedButton(
            onClick = onNavigateToBudget,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.home_action_add_expense))
        }
    }
}

private fun formatMoney(amountMinor: Long): String {
    val major = amountMinor / 100
    val minor = Math.abs(amountMinor % 100)
    return String.format(java.util.Locale.getDefault(), "₱%,d.%02d", major, minor)
}

@StringRes
private fun getGreetingByTimeOfDayResId(): Int {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..17 -> R.string.home_greeting_afternoon
        else -> R.string.home_greeting_evening
    }
}

// Supporting data classes
data class HomeNoteItem(
    val id: String,
    val title: String,
    val preview: String,
    val updatedTime: String
)

data class HomeActivityItem(
    val id: String,
    val description: String,
    val time: String
)
