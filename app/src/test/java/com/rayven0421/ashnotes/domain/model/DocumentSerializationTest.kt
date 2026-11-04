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

        val originalDocument = Document(
            version = 1,
            blocks = listOf(paragraphBlock, headingBlock, checklistBlock)
        )

        val jsonString = json.encodeToString(Document.serializer(), originalDocument)
        val deserializedDocument = json.decodeFromString(Document.serializer(), jsonString)

        assertEquals(originalDocument, deserializedDocument)
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
}
