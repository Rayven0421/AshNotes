package com.rayven0421.ashnotes.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shared Screen Header composable for AshNotes.
 * Used across Home (greeting), Notes, Budget, and Settings for consistent
 * positioning, typography style, and spacing.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = AshNotesTheme.spacing.large,
                bottom = AshNotesTheme.spacing.medium
            )
    )
}
