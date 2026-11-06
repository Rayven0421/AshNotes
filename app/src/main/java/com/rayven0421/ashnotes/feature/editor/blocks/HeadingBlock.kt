package com.rayven0421.ashnotes.feature.editor.blocks

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.domain.model.Block
import com.rayven0421.ashnotes.domain.model.FocusRequest
import com.rayven0421.ashnotes.feature.editor.BlockContainer
import com.rayven0421.ashnotes.feature.editor.BlockTextField

@Composable
fun HeadingBlockItem(
    block: Block.Heading,
    focusRequest: FocusRequest?,
    onTextChanged: (String) -> Unit,
    onEnterPressed: (Int) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onPasteMultiLine: (String, Int) -> Unit,
    onFocused: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    BlockContainer(
        isFocused = isFocused,
        modifier = modifier
    ) {
        BlockTextField(
            blockId = block.id,
            blockText = block.text,
            focusRequest = focusRequest,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            onTextChanged = onTextChanged,
            onSplit = onEnterPressed,
            onBackspaceAtStart = onBackspaceAtStart,
            onPaste = onPasteMultiLine,
            onFocused = onFocused,
            placeholder = stringResource(R.string.editor_heading_placeholder),
            onFocusStateChanged = { isFocused = it }
        )
    }
}
