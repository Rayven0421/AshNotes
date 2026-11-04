package com.rayven0421.ashnotes.core.designsystem.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

@Preview(name = "Ash Confirm Dialog - Normal", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun AshConfirmDialogPreview() {
    AshNotesTheme {
        AshConfirmDialog(
            title = "Archive note",
            message = "This note will be moved to archives. You can unarchive it anytime.",
            confirmLabel = "Archive",
            isDestructive = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Ash Confirm Dialog - Destructive & Long Text", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun AshConfirmDialogDestructiveLongPreview() {
    AshNotesTheme {
        AshConfirmDialog(
            title = "Delete selected notes?",
            message = "Are you sure you want to permanently delete 5 selected notes? This action is irreversible and all associated blocks and local references will be removed from your workspace immediately.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Ash Input Dialog - Rename", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun AshTextInputDialogPreview() {
    AshNotesTheme {
        AshTextInputDialog(
            title = "Rename note",
            label = "Title",
            initialValue = "Grocery run & supplies",
            confirmLabel = "Save",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Ash Input Dialog - Long Text & Supporting Text", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun AshTextInputDialogLongTextPreview() {
    AshNotesTheme {
        AshTextInputDialog(
            title = "Create new note in workspace",
            supportingText = "Please enter a title for your note. If left blank, it will be saved as Untitled.",
            label = "Title",
            initialValue = "Project Roadmap 2026 - Quarter 4 Architecture and Feature Specification",
            confirmLabel = "Create",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
