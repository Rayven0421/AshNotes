package com.rayven0421.ashnotes.feature.notes

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.ScreenHeader

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<NotesContent> = UiState.Empty,
    onSearchQueryChanged: (String) -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    onNewNoteClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (uiState is UiState.Success) {
                FloatingActionButton(
                    onClick = onNewNoteClick,
                    containerColor = AshNotesTheme.colors.ember,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.notes_new_note)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AshNotesTheme.spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.medium)
        ) {
            ScreenHeader(title = stringResource(R.string.notes_screen_title))

            // Always-visible search bar & compact sort/filter control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.default),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.notes_search_hint),
                            color = AshNotesTheme.colors.textMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = AshNotesTheme.colors.textMuted
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AshNotesTheme.colors.ember,
                        unfocusedBorderColor = AshNotesTheme.colors.line,
                        focusedContainerColor = AshNotesTheme.colors.raised,
                        unfocusedContainerColor = AshNotesTheme.colors.raised,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                IconButton(
                    onClick = { /* Compact sort/filter action */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.notes_sort_filter),
                        tint = AshNotesTheme.colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(AshNotesTheme.spacing.extraSmall))

            // Content based on UiState
            when (uiState) {
                is UiState.Empty, is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
                        ) {
                            Text(
                                text = stringResource(R.string.notes_empty_message),
                                style = MaterialTheme.typography.bodyLarge,
                                color = AshNotesTheme.colors.textMuted
                            )
                            OutlinedButton(onClick = onNewNoteClick) {
                                Text(text = stringResource(R.string.notes_new_note))
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
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
                    val pinned = content.pinnedNotes
                    val others = content.otherNotes

                    if (pinned.isEmpty() && others.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
                            ) {
                                Text(
                                    text = stringResource(R.string.notes_empty_message),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = AshNotesTheme.colors.textMuted
                                )
                                OutlinedButton(onClick = onNewNoteClick) {
                                    Text(text = stringResource(R.string.notes_new_note))
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            // Pinned Group
                            if (pinned.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.notes_pinned_header),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AshNotesTheme.colors.textMuted,
                                        modifier = Modifier.padding(bottom = AshNotesTheme.spacing.small)
                                    )
                                }
                                itemsIndexed(pinned) { index, note ->
                                    NoteRow(note = note, onClick = { onNoteClick(note.id) })
                                    if (index < pinned.size - 1 || others.isNotEmpty()) {
                                        HorizontalDivider(
                                            color = AshNotesTheme.colors.line,
                                            thickness = 1.dp
                                        )
                                    }
                                }
                                item {
                                    Spacer(modifier = Modifier.height(AshNotesTheme.spacing.large))
                                }
                            }

                            // Other Notes Group
                            if (others.isNotEmpty()) {
                                if (pinned.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = stringResource(R.string.notes_other_header),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AshNotesTheme.colors.textMuted,
                                            modifier = Modifier.padding(bottom = AshNotesTheme.spacing.small)
                                        )
                                    }
                                }
                                itemsIndexed(others) { index, note ->
                                    NoteRow(note = note, onClick = { onNoteClick(note.id) })
                                    if (index < others.size - 1) {
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
            }
        }
    }
}

@Composable
private fun NoteRow(
    note: NoteItem,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = AshNotesTheme.colors.ember,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(AshNotesTheme.spacing.default))
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

// Sample* data used ONLY by @Preview functions per instructions
private object SampleNotesData {
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
