package com.rayven0421.ashnotes.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

internal object SampleHomeData {
    val successState = HomeContent(
        greeting = "Good afternoon",
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
        activity = listOf(
            HomeActivityItem(
                id = "a1",
                description = "Added expense: Coffee beans (₱450.00)",
                time = "1h ago"
            ),
            HomeActivityItem(
                id = "a2",
                description = "Updated note: Grocery run",
                time = "2h ago"
            )
        ),
        syncStatus = "Local only"
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
