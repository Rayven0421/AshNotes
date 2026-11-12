package com.rayven0421.ashnotes.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class NotePreviewExtractorTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun extractNotePreview_paragraphFirst_returnsParagraphText() {
        val document = Document(
            version = 1,
            blocks = listOf(
                Block.Paragraph(
                    id = "p1",
                    position = 0,
                    createdAt = 1000L,
                    updatedAt = 1000L,
                    text = "This is a paragraph preview text."
                )
            )
        )
        val jsonString = json.encodeToString(Document.serializer(), document)
        val preview = extractNotePreview(jsonString)

        assertEquals("This is a paragraph preview text.", preview)
    }

    @Test
    fun extractNotePreview_headingFirst_returnsHeadingText() {
        val document = Document(
            version = 1,
            blocks = listOf(
                Block.Heading(
                    id = "h1",
                    position = 0,
                    createdAt = 1000L,
                    updatedAt = 1000L,
                    text = "Meeting Agenda",
                    level = 1
                )
            )
        )
        val jsonString = json.encodeToString(Document.serializer(), document)
        val preview = extractNotePreview(jsonString)

        assertEquals("Meeting Agenda", preview)
    }

    @Test
    fun extractNotePreview_skipsBlankBlocks_returnsFirstNonBlank() {
        val document = Document(
            version = 1,
            blocks = listOf(
                Block.Paragraph(
                    id = "p1",
                    position = 0,
                    createdAt = 1000L,
                    updatedAt = 1000L,
                    text = "   \n  "
                ),
                Block.Quote(
                    id = "q1",
                    position = 1,
                    createdAt = 1001L,
                    updatedAt = 1001L,
                    text = "First non-blank quote."
                )
            )
        )
        val jsonString = json.encodeToString(Document.serializer(), document)
        val preview = extractNotePreview(jsonString)

        assertEquals("First non-blank quote.", preview)
    }

    @Test
    fun extractNotePreview_checklist_returnsChecklistText() {
        val document = Document(
            version = 1,
            blocks = listOf(
                Block.Checklist(
                    id = "c1",
                    position = 0,
                    createdAt = 1000L,
                    updatedAt = 1000L,
                    text = "Buy groceries",
                    isChecked = false
                )
            )
        )
        val jsonString = json.encodeToString(Document.serializer(), document)
        val preview = extractNotePreview(jsonString)

        assertEquals("Buy groceries", preview)
    }

    @Test
    fun extractNotePreview_dividerOnly_returnsEmptyString() {
        val document = Document(
            version = 1,
            blocks = listOf(
                Block.Divider(
                    id = "d1",
                    position = 0,
                    createdAt = 1000L,
                    updatedAt = 1000L
                )
            )
        )
        val jsonString = json.encodeToString(Document.serializer(), document)
        val preview = extractNotePreview(jsonString)

        assertEquals("", preview)
    }

    @Test
    fun extractNotePreview_invalidJson_returnsEmptyStringAndDoesNotThrow() {
        val invalidJson = "{ invalid json data [[[ "
        val preview = extractNotePreview(invalidJson)

        assertEquals("", preview)
    }

    @Test
    fun extractNotePreview_longTextTruncation_collapsesWhitespaceAndTruncatesTo120() {
        val longText = "   This is a very long paragraph with extra   spaces and line\nbreaks that exceeds one hundred and twenty characters in total length for testing truncation properly.   "
        val document = Document(
            version = 1,
            blocks = listOf(
                Block.Paragraph(
                    id = "p1",
                    position = 0,
                    createdAt = 1000L,
                    updatedAt = 1000L,
                    text = longText
                )
            )
        )
        val jsonString = json.encodeToString(Document.serializer(), document)
        val preview = extractNotePreview(jsonString)

        val expectedCollapsed = "This is a very long paragraph with extra spaces and line breaks that exceeds one hundred and twenty characters in total length for testing truncation properly."
        val expectedTruncated = expectedCollapsed.take(120)

        assertEquals(120, preview.length)
        assertEquals(expectedTruncated, preview)
    }
}
