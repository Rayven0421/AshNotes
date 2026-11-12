package com.rayven0421.ashnotes.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.FocusRequest
import com.rayven0421.ashnotes.domain.model.TextChange
import com.rayven0421.ashnotes.domain.model.TextChangeInterpreter
import com.rayven0421.ashnotes.domain.model.TextSyncEngine

@Composable
fun BlockContainer(
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(
                    if (isFocused) AshNotesTheme.colors.ember else Color.Transparent,
                    RoundedCornerShape(1.dp)
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopStart
        ) {
            content()
        }
    }
}

@Composable
fun BlockTextField(
    blockId: String,
    blockText: String,
    focusRequest: FocusRequest?,
    textStyle: TextStyle,
    onTextChanged: (String) -> Unit,
    onSplit: (Int) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onPaste: (String, Int) -> Unit,
    onFocused: (Int) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    onFocusStateChanged: ((Boolean) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    var recentEmittedTexts by remember(blockId) { mutableStateOf(listOf(blockText)) }
    var lastAppliedFocusToken by remember(blockId) { mutableStateOf<Long?>(null) }

    val effectiveFocusRequest = if (focusRequest != null && focusRequest.token != lastAppliedFocusToken) {
        focusRequest
    } else {
        null
    }

    val initialSelection = if (effectiveFocusRequest != null) {
        TextRange(effectiveFocusRequest.cursor.coerceIn(0, blockText.length))
    } else {
        TextRange(blockText.length)
    }

    var textFieldValue by remember(blockId) {
        mutableStateOf(
            TextFieldValue(
                text = blockText,
                selection = initialSelection
            )
        )
    }

    LaunchedEffect(blockText, focusRequest?.token) {
        val req = if (focusRequest != null && focusRequest.token != lastAppliedFocusToken) {
            focusRequest
        } else {
            null
        }

        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = textFieldValue.text,
            currentCaret = textFieldValue.selection.start,
            incomingBlockText = blockText,
            recentEmittedTexts = recentEmittedTexts,
            focusRequest = req
        )
        recentEmittedTexts = decision.updatedRecentEmittedTexts

        if (decision.shouldResync) {
            textFieldValue = TextFieldValue(
                text = decision.targetText,
                selection = TextRange(decision.targetCaret)
            )
            if (req != null) {
                lastAppliedFocusToken = req.token
            }
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            when (val change = TextChangeInterpreter.interpret(blockText, newValue.text)) {
                is TextChange.Plain -> {
                    textFieldValue = newValue
                    if (change.newText != blockText) {
                        recentEmittedTexts = (recentEmittedTexts + change.newText).takeLast(16)
                        onTextChanged(change.newText)
                    }
                    onFocused(newValue.selection.start)
                }
                is TextChange.Enter -> {
                    if (change.textBefore != blockText) {
                        recentEmittedTexts = (recentEmittedTexts + change.textBefore).takeLast(16)
                        onTextChanged(change.textBefore)
                    }
                    onSplit(change.cursor)
                }
                is TextChange.Paste -> {
                    if (change.textBefore != blockText) {
                        recentEmittedTexts = (recentEmittedTexts + change.textBefore).takeLast(16)
                        onTextChanged(change.textBefore)
                    }
                    onPaste(change.inserted, change.cursor)
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusStateChanged?.invoke(focusState.isFocused)
                if (focusState.isFocused) {
                    onFocused(textFieldValue.selection.start)
                }
            }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                    if (textFieldValue.selection.collapsed && textFieldValue.selection.start == 0) {
                        onBackspaceAtStart()
                        true
                    } else false
                } else false
            },
        textStyle = textStyle,
        cursorBrush = SolidColor(AshNotesTheme.colors.ember),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (placeholder != null && blockText.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = AshNotesTheme.colors.textMuted.copy(alpha = EditorTokens.PlaceholderAlpha)
                    )
                }
                innerTextField()
            }
        }
    )
}
