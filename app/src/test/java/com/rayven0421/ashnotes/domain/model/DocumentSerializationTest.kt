package com.rayven0421.ashnotes.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    @Test
    fun documentRoundTripSerialization_preservesAllBlockTypes() {
        val paragraphBlock = Block.Paragraph(
            id = "b1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            metadata = mapOf("author" to "ash"),
            text = "Hello world",
            spans = listOf(
                TextSpan(start = 0, end = 5, style = SpanStyle.BOLD)
            )
        )

        val headingBlock = Block.Heading(
            id = "b2",
            position = 1,
            createdAt = 1001L,
            updatedAt = 1001L,
            text = "Chapter 1",
            level = 1
        )

        val checklistBlock = Block.Checklist(
            id = "b3",
            position = 2,
            createdAt = 1002L,
            updatedAt = 1002L,
            items = listOf(
                ChecklistItem(id = "item1", text = "Buy groceries", isChecked = true),
                ChecklistItem(id = "item2", text = "Write code", isChecked = false)
            )
        )

        val bulletBlock = Block.BulletItem(
            id = "b4",
            position = 3,
            createdAt = 1003L,
            updatedAt = 1003L,
            text = "Bullet item 1"
        )

        val numberedBlock = Block.NumberedItem(
            id = "b5",
            position = 4,
            createdAt = 1004L,
            updatedAt = 1004L,
            text = "Numbered item 1"
        )

        val quoteBlock = Block.Quote(
            id = "b6",
            position = 5,
            createdAt = 1005L,
            updatedAt = 1005L,
            text = "Quote text"
        )

        val dividerBlock = Block.Divider(
            id = "b7",
            position = 6,
            createdAt = 1006L,
            updatedAt = 1006L
        )

        val originalDocument = Document(
            version = 1,
            blocks = listOf(paragraphBlock, headingBlock, checklistBlock, bulletBlock, numberedBlock, quoteBlock, dividerBlock)
        )

        val jsonString = json.encodeToString(Document.serializer(), originalDocument)
        val deserializedDocument = json.decodeFromString(Document.serializer(), jsonString)

        assertEquals(originalDocument, deserializedDocument)
        assertTrue(deserializedDocument.blocks[3] is Block.BulletItem)
        assertTrue(deserializedDocument.blocks[4] is Block.NumberedItem)
        assertTrue(deserializedDocument.blocks[5] is Block.Quote)
        assertTrue(deserializedDocument.blocks[6] is Block.Divider)
    }

    @Test
    fun unknownBlockTypeSurvives_withoutDataLoss() {
        val jsonWithUnknownBlock = """
            {
                "version": 1,
                "blocks": [
                    {
                        "type": "future_table_block",
                        "id": "b_unknown_1",
                        "position": 0,
                        "createdAt": 2000,
                        "updatedAt": 2000,
                        "metadata": { "custom": "field" },
                        "columns": 3,
                        "rows": [["a", "b", "c"]]
                    }
                ]
            }
        """.trimIndent()

        val decodedDocument = json.decodeFromString(Document.serializer(), jsonWithUnknownBlock)

        assertEquals(1, decodedDocument.blocks.size)
        val unknownBlock = decodedDocument.blocks[0]
        assertTrue(unknownBlock is Block.UnknownBlock)

        val unknown = unknownBlock as Block.UnknownBlock
        assertEquals("b_unknown_1", unknown.id)
        assertEquals("future_table_block", unknown.rawType)
        assertEquals("field", unknown.metadata["custom"])

        // Re-serialize back to JSON string and verify original type and unknown fields are preserved
        val reSerializedJson = json.encodeToString(Document.serializer(), decodedDocument)
        val reDecodedDocument = json.decodeFromString(Document.serializer(), reSerializedJson)

        assertEquals(decodedDocument, reDecodedDocument)
        assertTrue(reSerializedJson.contains("\"type\":\"future_table_block\""))
        assertTrue(reSerializedJson.contains("\"columns\":3"))
    }

    @Test
    fun olderDocumentVersionLoads_withDefaults() {
        val olderVersionJson = """
            {
                "version": 1,
                "blocks": [
                    {
                        "type": "paragraph",
                        "id": "old_p1",
                        "position": 0,
                        "createdAt": 500,
                        "updatedAt": 500,
                        "text": "Simple paragraph without spans or metadata"
                    }
                ]
            }
        """.trimIndent()

        val decodedDocument = json.decodeFromString(Document.serializer(), olderVersionJson)

        assertEquals(1, decodedDocument.version)
        assertEquals(1, decodedDocument.blocks.size)

        val block = decodedDocument.blocks[0] as Block.Paragraph
        assertEquals("old_p1", block.id)
        assertEquals("Simple paragraph without spans or metadata", block.text)
        assertTrue(block.spans.isEmpty())
        assertTrue(block.metadata.isEmpty())
    }

    @Test
    fun decodeCurrentVersionSavedJson_parsesTextBlocksAndSupportsWithText() {
        val currentSavedJson = """
            {
                "version": 1,
                "blocks": [
                    {
                        "type": "paragraph",
                        "id": "p1",
                        "position": 0,
                        "createdAt": 1000,
                        "updatedAt": 1000,
                        "metadata": {},
                        "text": "Paragraph text",
                        "spans": []
                    },
                    {
                        "type": "heading",
                        "id": "h1",
                        "position": 1,
                        "createdAt": 1000,
                        "updatedAt": 1000,
                        "metadata": {},
                        "text": "Heading text",
                        "level": 1,
                        "spans": []
                    },
                    {
                        "type": "checklist",
                        "id": "c1",
                        "position": 2,
                        "createdAt": 1000,
                        "updatedAt": 1000,
                        "metadata": {},
                        "text": "Checklist text",
                        "isChecked": true,
                        "items": [],
                        "spans": []
                    }
                ]
            }
        """.trimIndent()

        val decodedDocument = json.decodeFromString(Document.serializer(), currentSavedJson)

        assertEquals(3, decodedDocument.blocks.size)

        val textBlock1 = decodedDocument.blocks[0] as TextBlock
        val textBlock2 = decodedDocument.blocks[1] as TextBlock
        val textBlock3 = decodedDocument.blocks[2] as TextBlock

        assertTrue(textBlock1 is Block.Paragraph)
        assertEquals("Paragraph text", textBlock1.text)

        assertTrue(textBlock2 is Block.Heading)
        assertEquals("Heading text", textBlock2.text)

        assertTrue(textBlock3 is Block.Checklist)
        assertEquals("Checklist text", textBlock3.text)
        assertTrue((textBlock3 as Block.Checklist).isChecked)

        // Test withText on TextBlock interface
        val updated1 = textBlock1.withText("New P", 2000L)
        val updated2 = textBlock2.withText("New H", 2000L)
        val updated3 = textBlock3.withText("New C", 2000L)

        assertEquals("New P", updated1.text)
        assertEquals(2000L, updated1.updatedAt)

        assertEquals("New H", updated2.text)
        assertEquals(2000L, updated2.updatedAt)

        assertEquals("New C", updated3.text)
        assertEquals(2000L, updated3.updatedAt)
    }
}
