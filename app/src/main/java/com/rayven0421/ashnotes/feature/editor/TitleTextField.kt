package com.rayven0421.ashnotes.feature.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

@Composable
fun TitleTextField(
    title: String,
    onTitleChanged: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = title,
        onValueChange = onTitleChanged,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.displayMedium.copy(
            color = MaterialTheme.colorScheme.onBackground
        ),
        cursorBrush = SolidColor(AshNotesTheme.colors.ember),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        maxLines = 2,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (title.isEmpty()) {
                    Text(
                        text = stringResource(R.string.editor_title_placeholder),
                        style = MaterialTheme.typography.displayMedium,
                        color = AshNotesTheme.colors.textMuted
                    )
                }
                innerTextField()
            }
        }
    )
}
