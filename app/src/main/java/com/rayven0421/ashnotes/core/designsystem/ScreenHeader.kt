package com.rayven0421.ashnotes.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shared Screen Header composable for AshNotes.
 * Used across Home, Notes, Budget, and Settings for consistent
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
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Shared layout container for all four main tab screens (Home, Notes, Budget, Settings).
 * Enforces identical top title offset, horizontal padding token, title-to-content gap,
 * and bottom inset clearing for consistent tab screen layout across the app.
 */
@Composable
fun TabScreenLayout(
    title: String,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            if (snackbarHostState != null) {
                SnackbarHost(hostState = snackbarHostState)
            }
        },
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    top = AshNotesTheme.spacing.large,
                    start = AshNotesTheme.spacing.screenHorizontal,
                    end = AshNotesTheme.spacing.screenHorizontal
                ),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
        ) {
            ScreenHeader(title = title)
            content()
        }
    }
}

