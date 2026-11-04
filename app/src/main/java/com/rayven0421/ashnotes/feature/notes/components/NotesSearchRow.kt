package com.rayven0421.ashnotes.feature.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.NoteSortOption

@Composable
fun NotesSearchRow(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    currentSortOption: NoteSortOption,
    onSortOptionSelected: (NoteSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
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
                    contentDescription = stringResource(R.string.notes_search_hint),
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

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier
                    .size(56.dp)
                    .background(AshNotesTheme.colors.raised, MaterialTheme.shapes.small)
                    .border(1.dp, AshNotesTheme.colors.line, MaterialTheme.shapes.small)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                    contentDescription = stringResource(R.string.notes_sort_label),
                    tint = if (currentSortOption != NoteSortOption.UPDATED) AshNotesTheme.colors.ember else AshNotesTheme.colors.textMuted
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(AshNotesTheme.colors.raised)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.notes_sort_updated)) },
                    onClick = {
                        onSortOptionSelected(NoteSortOption.UPDATED)
                        showMenu = false
                    },
                    trailingIcon = {
                        if (currentSortOption == NoteSortOption.UPDATED) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = AshNotesTheme.colors.ember)
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.notes_sort_created)) },
                    onClick = {
                        onSortOptionSelected(NoteSortOption.CREATED)
                        showMenu = false
                    },
                    trailingIcon = {
                        if (currentSortOption == NoteSortOption.CREATED) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = AshNotesTheme.colors.ember)
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.notes_sort_title_asc)) },
                    onClick = {
                        onSortOptionSelected(NoteSortOption.TITLE_ASC)
                        showMenu = false
                    },
                    trailingIcon = {
                        if (currentSortOption == NoteSortOption.TITLE_ASC) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = AshNotesTheme.colors.ember)
                        }
                    }
                )
            }
        }
    }
}
