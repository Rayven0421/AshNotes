package com.rayven0421.ashnotes.feature.editor.blocks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.Block
import com.rayven0421.ashnotes.domain.model.FocusRequest
import com.rayven0421.ashnotes.feature.editor.BlockContainer
import com.rayven0421.ashnotes.feature.editor.BlockTextField
import com.rayven0421.ashnotes.feature.editor.EditorTokens

@Composable
fun NumberedBlockItem(
    block: Block.NumberedItem,
    number: Int,
    focusRequest: FocusRequest?,
    onTextChanged: (String) -> Unit,
    onEnterPressed: (Int) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onPasteMultiLine: (String, Int) -> Unit,
    onFocused: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onBackground
    )
    val numberStyle = MaterialTheme.typography.bodyLarge.copy(
        color = AshNotesTheme.colors.textMuted,
        fontFeatureSettings = "tnum"
    )

    BlockContainer(
        isFocused = isFocused,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(EditorTokens.ListGutterWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = "$number.",
                    style = numberStyle
                )
            }

            BlockTextField(
                blockId = block.id,
                blockText = block.text,
                focusRequest = focusRequest,
                textStyle = textStyle,
                onTextChanged = onTextChanged,
                onSplit = onEnterPressed,
                onBackspaceAtStart = onBackspaceAtStart,
                onPaste = onPasteMultiLine,
                onFocused = onFocused,
                onFocusStateChanged = { isFocused = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("numbered_text")
            )
        }
    }
}
