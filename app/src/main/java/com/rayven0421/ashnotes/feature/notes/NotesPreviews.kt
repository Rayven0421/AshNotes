package com.rayven0421.ashnotes.feature.notes

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

internal object SampleNotesData {
    val successState = NotesContent(
        pinnedNotes = listOf(
            NoteItem(
                id = "p1",
                title = "2026 Financial & Investment Strategy",
                preview = "Diversify index funds, evaluate treasury yields, and maintain emergency reserve.",
                updatedTime = "Today",
                isPinned = true
            )
        ),
        otherNotes = listOf(
            NoteItem(
                id = "n1",
                title = "Grocery run & supplies",
                preview = "Coffee beans, oat milk, sourdough bread from organic bakery.",
                updatedTime = "2h ago"
            ),
            NoteItem(
                id = "n2",
                title = "Apartment utility budget",
                preview = "Electricity and fiber internet bill breakdown for November.",
                updatedTime = "Yesterday"
            ),
            NoteItem(
                id = "n3",
                title = "Books to read",
                preview = "Designing Data-Intensive Applications, Clean Architecture notes.",
                updatedTime = "3d ago"
            )
        )
    )
}

@Preview(name = "Notes - Success", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun NotesScreenSuccessPreview() {
    AshNotesTheme {
        NotesScreen(uiState = UiState.Success(SampleNotesData.successState))
    }
}

@Preview(name = "Notes - Empty", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun NotesScreenEmptyPreview() {
    AshNotesTheme {
        NotesScreen(uiState = UiState.Empty)
    }
}
