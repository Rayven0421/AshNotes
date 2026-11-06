package com.rayven0421.ashnotes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TextChangeInterpreterTest {

    @Test
    fun enterAtStart_returnsEnterWithZeroCursorAndOriginalTextBefore() {
        val oldText = "hello"
        val newText = "\nhello"

        val change = TextChangeInterpreter.interpret(oldText, newText)

        assertEquals(TextChange.Enter(cursor = 0, textBefore = "hello"), change)
    }

    @Test
    fun enterAtMiddle_returnsEnterWithMiddleCursorAndOriginalTextBefore() {
        val oldText = "hello"
        val newText = "hel\nlo"

        val change = TextChangeInterpreter.interpret(oldText, newText)

        assertEquals(TextChange.Enter(cursor = 3, textBefore = "hello"), change)
    }

    @Test
    fun enterAtEnd_returnsEnterWithEndCursorAndOriginalTextBefore() {
        val oldText = "hello"
        val newText = "hello\n"

        val change = TextChangeInterpreter.interpret(oldText, newText)

        assertEquals(TextChange.Enter(cursor = 5, textBefore = "hello"), change)
    }

    @Test
    fun enterReplacingSelection_returnsEnterWithPrefixCursorAndTextBeforeWithoutSelection() {
        // oldText = "hello", selection was "ll" (indices 2..4), replaced with '\n' -> "he\no"
        val oldText = "hello"
        val newText = "he\no"

        val change = TextChangeInterpreter.interpret(oldText, newText)

        assertEquals(TextChange.Enter(cursor = 2, textBefore = "heo"), change)
    }

    @Test
    fun pasteMultiLineIntoEmptyText_returnsPasteWithZeroCursorAndEmptyTextBefore() {
        val oldText = ""
        val newText = "a\nb\nc"

        val change = TextChangeInterpreter.interpret(oldText, newText)

        assertEquals(TextChange.Paste(cursor = 0, inserted = "a\nb\nc", textBefore = ""), change)
    }

    @Test
    fun pasteMultiLineIntoMiddleOfText_returnsPasteWithPrefixCursorInsertedAndTextBefore() {
        val oldText = "hello"
        val newText = "he\nworld\nllo"

        val change = TextChangeInterpreter.interpret(oldText, newText)

        assertEquals(TextChange.Paste(cursor = 2, inserted = "\nworld\n", textBefore = "hello"), change)
    }

    @Test
    fun pasteWithoutNewline_returnsPlainWithNewText() {
        val oldText = "hello"
        val newText = "heworldllo"

        val change = TextChangeInterpreter.interpret(oldText, newText)

        assertEquals(TextChange.Plain(newText = "heworldllo"), change)
    }

    @Test
    fun backspaceOnlyChange_returnsPlainWithNewText() {
        val oldText = "hello"
        val newText = "heo"

        val change = TextChangeInterpreter.interpret(oldText, newText)

        assertEquals(TextChange.Plain(newText = "heo"), change)
    }

    @Test
    fun imeCompositionReplacingWord_returnsPlainWithNewText() {
        val oldText = "hello"
        val newText = "world"

        val change = TextChangeInterpreter.interpret(oldText, newText)

        assertEquals(TextChange.Plain(newText = "world"), change)
    }
}
