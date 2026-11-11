package com.rayven0421.ashnotes.feature.editor.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.takeOrElse
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.Block
import com.rayven0421.ashnotes.domain.model.FocusRequest
import com.rayven0421.ashnotes.feature.editor.BlockContainer
import com.rayven0421.ashnotes.feature.editor.BlockTextField
import com.rayven0421.ashnotes.feature.editor.EditorTokens

@Composable
fun BulletBlockItem(
    block: Block.BulletItem,
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

    val density = LocalDensity.current
    val lineHeightDp = with(density) {
        textStyle.lineHeight.takeOrElse { textStyle.fontSize }.toDp()
    }
    val bulletTopPadding = ((lineHeightDp - EditorTokens.BulletSize) / 2).coerceAtLeast(AshNotesTheme.spacing.none)

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
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = bulletTopPadding)
                        .size(EditorTokens.BulletSize)
                        .clip(CircleShape)
                        .background(AshNotesTheme.colors.textMuted)
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
                    .testTag("bullet_text")
            )
        }
    }
}
