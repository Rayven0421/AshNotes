package com.rayven0421.ashnotes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistOperationsTest {

    @Test
    fun convertEmptyParagraphToChecklist_returnsOneEmptyChecklistBlock() {
        val paragraph = Block.Paragraph(
            id = "p1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = ""
        )

        val converted = ChecklistOperations.convertBlockToChecklist(paragraph, now = 2000L)

        assertEquals(1, converted.size)
        val checklist = converted[0] as Block.Checklist
        assertEquals("p1", checklist.id)
        assertEquals("", checklist.text)
        assertEquals(false, checklist.isChecked)
    }

    @Test
    fun convertParagraphWithBlankLinesToChecklist_dropsBlankLinesAndAssignsUniqueIds() {
        val paragraph = Block.Paragraph(
            id = "p1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = "Item 1\n\n   \nItem 2"
        )

        val converted = ChecklistOperations.convertBlockToChecklist(paragraph, now = 2000L)

        assertEquals(2, converted.size)
        val item1 = converted[0] as Block.Checklist
        val item2 = converted[1] as Block.Checklist

        assertEquals("p1", item1.id)
        assertEquals("Item 1", item1.text)

        assertTrue("Second item must have a unique ID different from p1", item2.id != "p1")
        assertEquals("Item 2", item2.text)
    }

    @Test
    fun convertMultiLineParagraphToChecklist_splitsIntoOneChecklistBlockPerLine() {
        val paragraph = Block.Paragraph(
            id = "p1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = "Milk\nBread\nEggs"
        )

        val converted = ChecklistOperations.convertBlockToChecklist(paragraph, now = 2000L)

        assertEquals(3, converted.size)
        assertTrue(converted.all { it is Block.Checklist })

        val item1 = converted[0] as Block.Checklist
        val item2 = converted[1] as Block.Checklist
        val item3 = converted[2] as Block.Checklist

        assertEquals("p1", item1.id)
        assertEquals("Milk", item1.text)

        assertEquals("Bread", item2.text)
        assertEquals("Eggs", item3.text)
    }

    @Test
    fun convertSingleLineParagraphToChecklist_createsSingleChecklistBlock() {
        val paragraph = Block.Paragraph(
            id = "p1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = "Single item"
        )

        val converted = ChecklistOperations.convertBlockToChecklist(paragraph, now = 2000L)

        assertEquals(1, converted.size)
        val checklist = converted[0] as Block.Checklist
        assertEquals("p1", checklist.id)
        assertEquals("Single item", checklist.text)
    }

    @Test
    fun convertChecklistToParagraph_convertsBackToParagraphBlock() {
        val checklist = Block.Checklist(
            id = "c1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = "Milk"
        )

        val converted = ChecklistOperations.convertBlockToChecklist(checklist, now = 2000L)

        assertEquals(1, converted.size)
        val paragraph = converted[0] as Block.Paragraph
        assertEquals("c1", paragraph.id)
        assertEquals("Milk", paragraph.text)
    }

    @Test
    fun documentBecomesEmpty_ensuresAtLeastOneParagraphBlock() {
        val emptyList = emptyList<Block>()
        val ensured = ChecklistOperations.ensureNonEmptyBlocks(emptyList, now = 2000L)

        assertEquals(1, ensured.size)
        assertTrue(ensured[0] is Block.Paragraph)
        assertEquals("", (ensured[0] as Block.Paragraph).text)
    }

    @Test
    fun splitChecklistBlock_splitsTextAtCursor_andInsertsNewChecklistBlock() {
        val item = Block.Checklist(
            id = "c1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = "Buy milk"
        )
        val blocks = listOf(item)

        // Split "Buy milk" at index 4 (after "Buy ")
        val result = ChecklistOperations.splitChecklistBlock(blocks, "c1", cursorIndex = 4, now = 2000L)

        assertEquals(2, result.blocks.size)
        val first = result.blocks[0] as Block.Checklist
        val second = result.blocks[1] as Block.Checklist

        assertEquals("Buy ", first.text)
        assertEquals("milk", second.text)
        assertEquals(second.id, result.focusBlockId)
        assertEquals(0, result.focusCursorIndex)
    }

    @Test
    fun splitChecklistBlock_onEmptyItem_convertsToParagraph() {
        val item = Block.Checklist(
            id = "c1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = ""
        )
        val blocks = listOf(item)

        val result = ChecklistOperations.splitChecklistBlock(blocks, "c1", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        val converted = result.blocks[0]
        assertTrue(converted is Block.Paragraph)
        assertEquals("c1", converted.id)
        assertEquals("", (converted as Block.Paragraph).text)
    }

    @Test
    fun mergeOrConvertOnBackspace_onEmptyChecklist_convertsToParagraph() {
        val item = Block.Checklist(
            id = "c1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = ""
        )
        val blocks = listOf(item)

        val result = ChecklistOperations.mergeOrConvertOnBackspace(blocks, "c1", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        val converted = result.blocks[0]
        assertTrue(converted is Block.Paragraph)
        assertEquals("c1", converted.id)
    }

    @Test
    fun mergeOrConvertOnBackspace_onNonEmptyChecklistAtCursorZero_mergesIntoPreviousChecklist() {
        val item1 = Block.Checklist(id = "c1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Buy ")
        val item2 = Block.Checklist(id = "c2", position = 1, createdAt = 1000L, updatedAt = 1000L, text = "milk")
        val blocks = listOf(item1, item2)

        val result = ChecklistOperations.mergeOrConvertOnBackspace(blocks, "c2", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        val merged = result.blocks[0] as Block.Checklist
        assertEquals("c1", merged.id)
        assertEquals("Buy milk", merged.text)
        assertEquals("c1", result.focusBlockId)
        assertEquals(4, result.focusCursorIndex)
    }

    @Test
    fun pasteMultiLineTextIntoChecklist_splitsIntoMultipleChecklistBlocks() {
        val item = Block.Checklist(id = "c1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val blocks = listOf(item)

        val pasted = "Apple\nBanana\nOrange"
        val result = ChecklistOperations.pasteTextIntoChecklist(blocks, "c1", pastedText = pasted, cursorIndex = 0, now = 2000L)

        assertEquals(3, result.blocks.size)
        val first = result.blocks[0] as Block.Checklist
        val second = result.blocks[1] as Block.Checklist
        val third = result.blocks[2] as Block.Checklist

        assertEquals("Apple", first.text)
        assertEquals("Banana", second.text)
        assertEquals("Orange", third.text)
        assertNotNull(result.focusBlockId)
    }

    @Test
    fun mapCaretForBlockConversion_multiLineParagraph_mapsCaretToTargetBlockAndRelativeCursor() {
        val paragraph = Block.Paragraph(
            id = "p1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = "Milk\nBread\nEggs"
        )
        val converted = ChecklistOperations.convertBlockToChecklist(paragraph, now = 2000L)
        // Cursor index 7 is inside "Bread" at index 2 ('e') because "Milk\n" is 5 chars
        val focusRequest = ChecklistOperations.mapCaretForBlockConversion(
            rawText = paragraph.text,
            convertedBlocks = converted,
            cursorIndex = 7
        )

        assertNotNull(focusRequest)
        assertEquals(converted[1].id, focusRequest?.blockId)
        assertEquals(2, focusRequest?.cursor)
    }
}
