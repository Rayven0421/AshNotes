package com.rayven0421.ashnotes.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

internal object SampleHomeData {
    val successState = HomeContent(
        greetingResId = R.string.home_greeting_afternoon,
        spentMinor = 325000L,
        totalMinor = 500000L,
        paceFraction = 0.6f,
        hasBudget = true,
        notes = listOf(
            HomeNoteItem(
                id = "1",
                title = "Grocery run & supplies",
                preview = "Coffee beans, oat milk, sourdough bread from organic bakery.",
                updatedTime = "2h ago"
            ),
            HomeNoteItem(
                id = "2",
                title = "Apartment utility budget",
                preview = "Electricity and fiber internet bill breakdown for November.",
                updatedTime = "Yesterday"
            )
        ),
        recentExpenses = listOf(
            HomeExpenseItem(
                id = "e1",
                title = "Supermarket groceries",
                amountMinor = 125050L,
                dateText = "Today"
            ),
            HomeExpenseItem(
                id = "e2",
                title = "Electricity bill",
                amountMinor = 240000L,
                dateText = "Yesterday"
            )
        ),
        userEmail = "user@example.com"
    )
}

@Preview(name = "Home - Success", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun HomeScreenSuccessPreview() {
    AshNotesTheme {
        HomeScreen(uiState = UiState.Success(SampleHomeData.successState))
    }
}

@Preview(name = "Home - Empty", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun HomeScreenEmptyPreview() {
    AshNotesTheme {
        HomeScreen(uiState = UiState.Empty)
    }
}
