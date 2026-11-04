package com.rayven0421.ashnotes.core.designsystem.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.rayven0421.ashnotes.core.designsystem.AshLargeShape
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

@Composable
fun AshDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .widthIn(max = AshNotesTheme.spacing.dialogMaxWidth)
                .fillMaxWidth(),
            shape = AshLargeShape,
            color = AshNotesTheme.colors.raised,
            border = BorderStroke(AshNotesTheme.spacing.hairline, AshNotesTheme.colors.line),
            tonalElevation = AshNotesTheme.spacing.none,
            shadowElevation = AshNotesTheme.spacing.none
        ) {
            Column(
                modifier = Modifier
                    .padding(AshNotesTheme.spacing.large),
                verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.default),
                content = content
            )
        }
    }
}
