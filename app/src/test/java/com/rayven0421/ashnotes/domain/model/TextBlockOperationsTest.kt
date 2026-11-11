package com.rayven0421.ashnotes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextBlockOperationsTest {

    @Test
    fun splitParagraphBlock_splitsTextAndCreatesParagraph() {
        val paragraph = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Hello world")
        val blocks = listOf(paragraph)

        val result = TextBlockOperations.split(blocks, "p1", cursorIndex = 5, now = 2000L)

        assertEquals(2, result.blocks.size)
        val first = result.blocks[0] as Block.Paragraph
        val second = result.blocks[1] as Block.Paragraph

        assertEquals("Hello", first.text)
        assertEquals(" world", second.text)
        assertEquals(second.id, result.focusBlockId)
        assertEquals(0, result.focusCursorIndex)
    }

    @Test
    fun splitHeadingBlock_splitsTextAndCreatesParagraph() {
        val heading = Block.Heading(id = "h1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Title text")
        val blocks = listOf(heading)

        val result = TextBlockOperations.split(blocks, "h1", cursorIndex = 5, now = 2000L)

        assertEquals(2, result.blocks.size)
        val first = result.blocks[0] as Block.Heading
        val second = result.blocks[1] as Block.Paragraph

        assertEquals("Title", first.text)
        assertEquals(" text", second.text)
        assertEquals(second.id, result.focusBlockId)
        assertEquals(0, result.focusCursorIndex)
    }

    @Test
    fun splitChecklistBlock_splitsTextAndCreatesChecklist() {
        val checklist = Block.Checklist(id = "c1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Buy milk")
        val blocks = listOf(checklist)

        val result = TextBlockOperations.split(blocks, "c1", cursorIndex = 4, now = 2000L)

        assertEquals(2, result.blocks.size)
        val first = result.blocks[0] as Block.Checklist
        val second = result.blocks[1] as Block.Checklist

        assertEquals("Buy ", first.text)
        assertEquals("milk", second.text)
        assertEquals(second.id, result.focusBlockId)
        assertEquals(0, result.focusCursorIndex)
    }

    @Test
    fun splitEmptyChecklist_convertsToParagraph() {
        val checklist = Block.Checklist(id = "c1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val blocks = listOf(checklist)

        val result = TextBlockOperations.split(blocks, "c1", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        val converted = result.blocks[0]
        assertTrue(converted is Block.Paragraph)
        assertEquals("c1", converted.id)
        assertEquals("", (converted as Block.Paragraph).text)
    }

    @Test
    fun mergeOnBackspace_paragraphIntoParagraph() {
        val p1 = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Hello ")
        val p2 = Block.Paragraph(id = "p2", position = 1, createdAt = 1000L, updatedAt = 1000L, text = "world")
        val blocks = listOf(p1, p2)

        val result = TextBlockOperations.mergeOnBackspace(blocks, "p2", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        val merged = result.blocks[0] as Block.Paragraph
        assertEquals("p1", merged.id)
        assertEquals("Hello world", merged.text)
        assertEquals("p1", result.focusBlockId)
        assertEquals(6, result.focusCursorIndex)
    }

    @Test
    fun mergeOnBackspace_emptyChecklist_convertsToParagraph() {
        val checklist = Block.Checklist(id = "c1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val blocks = listOf(checklist)

        val result = TextBlockOperations.mergeOnBackspace(blocks, "c1", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        val converted = result.blocks[0]
        assertTrue(converted is Block.Paragraph)
        assertEquals("c1", converted.id)
    }

    @Test
    fun pasteSingleLine_insertsTextIntoTargetBlock() {
        val p1 = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Hello world")
        val blocks = listOf(p1)

        val result = TextBlockOperations.paste(blocks, "p1", pastedText = " beautiful", cursorIndex = 5, now = 2000L)

        assertEquals(1, result.blocks.size)
        val updated = result.blocks[0] as Block.Paragraph
        assertEquals("Hello beautiful world", updated.text)
        assertEquals("p1", result.focusBlockId)
        assertEquals(15, result.focusCursorIndex)
    }

    @Test
    fun pasteMultiLineIntoParagraph_splitsIntoParagraphs() {
        val p1 = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Start End")
        val blocks = listOf(p1)

        val result = TextBlockOperations.paste(blocks, "p1", pastedText = "\nLine 1\nLine 2\n", cursorIndex = 5, now = 2000L)

        assertEquals(4, result.blocks.size)
        assertTrue(result.blocks.all { it is Block.Paragraph })
        assertEquals("Start", (result.blocks[0] as Block.Paragraph).text)
        assertEquals("Line 1", (result.blocks[1] as Block.Paragraph).text)
        assertEquals("Line 2", (result.blocks[2] as Block.Paragraph).text)
        assertEquals(" End", (result.blocks[3] as Block.Paragraph).text)
    }

    @Test
    fun pasteMultiLineIntoHeading_splitsIntoHeadingAndParagraphs() {
        val h1 = Block.Heading(id = "h1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Title End")
        val blocks = listOf(h1)

        val result = TextBlockOperations.paste(blocks, "h1", pastedText = "\nBody line 1\nBody line 2\n", cursorIndex = 5, now = 2000L)

        assertEquals(4, result.blocks.size)
        assertTrue(result.blocks[0] is Block.Heading)
        assertTrue(result.blocks[1] is Block.Paragraph)
        assertTrue(result.blocks[2] is Block.Paragraph)
        assertTrue(result.blocks[3] is Block.Paragraph)

        assertEquals("Title", (result.blocks[0] as Block.Heading).text)
        assertEquals("Body line 1", (result.blocks[1] as Block.Paragraph).text)
        assertEquals("Body line 2", (result.blocks[2] as Block.Paragraph).text)
        assertEquals(" End", (result.blocks[3] as Block.Paragraph).text)
    }

    @Test
    fun pasteMultiLineIntoChecklist_splitsIntoChecklistsFilteringBlankMiddleLines() {
        val c1 = Block.Checklist(id = "c1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val blocks = listOf(c1)

        val pasted = "Apple\n\n  \nBanana\nOrange"
        val result = TextBlockOperations.paste(blocks, "c1", pastedText = pasted, cursorIndex = 0, now = 2000L)

        assertEquals(3, result.blocks.size)
        assertTrue(result.blocks.all { it is Block.Checklist })
        assertEquals("Apple", (result.blocks[0] as Block.Checklist).text)
        assertEquals("Banana", (result.blocks[1] as Block.Checklist).text)
        assertEquals("Orange", (result.blocks[2] as Block.Checklist).text)
    }

    @Test
    fun convertChecklistToParagraph_preservesExactTextWithSpaces() {
        val checklist = Block.Checklist(id = "c1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Buy ")

        val converted = TextBlockOperations.convertTo(checklist, Block.Paragraph::class, now = 2000L)

        assertEquals(1, converted.size)
        val paragraph = converted[0] as Block.Paragraph
        assertEquals("c1", paragraph.id)
        assertEquals("Buy ", paragraph.text)
    }

    @Test
    fun convertSingleLineParagraphToChecklist_preservesExactTextWithSpaces() {
        val paragraph = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "  a ")

        val converted = TextBlockOperations.convertTo(paragraph, Block.Checklist::class, now = 2000L)

        assertEquals(1, converted.size)
        val checklist = converted[0] as Block.Checklist
        assertEquals("p1", checklist.id)
        assertEquals("  a ", checklist.text)
    }

    @Test
    fun convertMultiLineParagraphToChecklist_trimsAndDropsBlankLines() {
        val paragraph = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Milk\n  \nBread")

        val converted = TextBlockOperations.convertTo(paragraph, Block.Checklist::class, now = 2000L)

        assertEquals(2, converted.size)
        val c1 = converted[0] as Block.Checklist
        val c2 = converted[1] as Block.Checklist

        assertEquals("p1", c1.id)
        assertEquals("Milk", c1.text)
        assertEquals("Bread", c2.text)
    }

    @Test
    fun toggleChecklistBlock_withMissingOrNonTextBlockId_returnsEarlyWithoutFocusRequest() {
        val unknown = Block.UnknownBlock(id = "u1", position = 0, createdAt = 1000L, updatedAt = 1000L, rawType = "unknown")
        val blocks = listOf(unknown)

        val resultMissing = TextBlockOperations.toggleChecklistBlock(blocks, "non_existent_id", cursorIndex = 0, now = 2000L)
        assertEquals(blocks, resultMissing.first)
        assertNull(resultMissing.second)

        val resultUnknown = TextBlockOperations.toggleChecklistBlock(blocks, "u1", cursorIndex = 0, now = 2000L)
        assertEquals(blocks, resultUnknown.first)
        assertNull(resultUnknown.second)
    }

    @Test
    fun mapCaretForBlockConversion_multiLineText_calculatesTargetLineAndRelativeCursor() {
        val rawText = "Milk\nBread\nEggs"
        val p = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = rawText)
        val converted = TextBlockOperations.convertTo(p, Block.Checklist::class, now = 2000L)

        // Index 7 is inside "Bread" at offset 2 ('e')
        val focusRequest = TextBlockOperations.mapCaretForBlockConversion(
            rawText = rawText,
            convertedBlocks = converted,
            cursorIndex = 7
        )

        assertNotNull(focusRequest)
        assertEquals(converted[1].id, focusRequest?.blockId)
        assertEquals(2, focusRequest?.cursor)
    }

    @Test
    fun repositionBlocks_reindexesPositionsSequentially() {
        val b1 = Block.Paragraph(id = "b1", position = 10, createdAt = 1000L, updatedAt = 1000L, text = "A")
        val b2 = Block.Heading(id = "b2", position = 20, createdAt = 1000L, updatedAt = 1000L, text = "B")
        val b3 = Block.Checklist(id = "b3", position = 30, createdAt = 1000L, updatedAt = 1000L, text = "C")

        val repositioned = TextBlockOperations.repositionBlocks(listOf(b1, b2, b3))

        assertEquals(0, repositioned[0].position)
        assertEquals(1, repositioned[1].position)
        assertEquals(2, repositioned[2].position)
    }

    @Test
    fun splitBulletBlock_splitsTextAndCreatesBulletItem() {
        val bullet = Block.BulletItem(id = "b1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "First item")
        val result = TextBlockOperations.split(listOf(bullet), "b1", cursorIndex = 5, now = 2000L)

        assertEquals(2, result.blocks.size)
        val first = result.blocks[0] as Block.BulletItem
        val second = result.blocks[1] as Block.BulletItem
        assertEquals("First", first.text)
        assertEquals(" item", second.text)
        assertEquals(second.id, result.focusBlockId)
    }

    @Test
    fun splitEmptyBulletBlock_convertsToParagraph() {
        val bullet = Block.BulletItem(id = "b1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val result = TextBlockOperations.split(listOf(bullet), "b1", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        assertTrue(result.blocks[0] is Block.Paragraph)
        assertEquals("b1", result.blocks[0].id)
    }

    @Test
    fun splitNumberedBlock_splitsTextAndCreatesNumberedItem() {
        val numbered = Block.NumberedItem(id = "n1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "First item")
        val result = TextBlockOperations.split(listOf(numbered), "n1", cursorIndex = 5, now = 2000L)

        assertEquals(2, result.blocks.size)
        val first = result.blocks[0] as Block.NumberedItem
        val second = result.blocks[1] as Block.NumberedItem
        assertEquals("First", first.text)
        assertEquals(" item", second.text)
        assertEquals(second.id, result.focusBlockId)
    }

    @Test
    fun splitEmptyNumberedBlock_convertsToParagraph() {
        val numbered = Block.NumberedItem(id = "n1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val result = TextBlockOperations.split(listOf(numbered), "n1", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        assertTrue(result.blocks[0] is Block.Paragraph)
        assertEquals("n1", result.blocks[0].id)
    }

    @Test
    fun splitQuoteBlock_splitsTextAndCreatesQuote() {
        val quote = Block.Quote(id = "q1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Wise words")
        val result = TextBlockOperations.split(listOf(quote), "q1", cursorIndex = 4, now = 2000L)

        assertEquals(2, result.blocks.size)
        val first = result.blocks[0] as Block.Quote
        val second = result.blocks[1] as Block.Quote
        assertEquals("Wise", first.text)
        assertEquals(" words", second.text)
        assertEquals(second.id, result.focusBlockId)
    }

    @Test
    fun splitEmptyQuoteBlock_convertsToParagraph() {
        val quote = Block.Quote(id = "q1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val result = TextBlockOperations.split(listOf(quote), "q1", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        assertTrue(result.blocks[0] is Block.Paragraph)
        assertEquals("q1", result.blocks[0].id)
    }

    @Test
    fun mergeOnBackspace_emptyBullet_convertsToParagraph() {
        val bullet = Block.BulletItem(id = "b1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val result = TextBlockOperations.mergeOnBackspace(listOf(bullet), "b1", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        assertTrue(result.blocks[0] is Block.Paragraph)
        assertEquals("b1", result.blocks[0].id)
    }

    @Test
    fun mergeOnBackspace_paragraphAfterDivider_deletesDividerAndKeepsCaretAtStart() {
        val divider = Block.Divider(id = "d1", position = 0, createdAt = 1000L, updatedAt = 1000L)
        val paragraph = Block.Paragraph(id = "p1", position = 1, createdAt = 1000L, updatedAt = 1000L, text = "Paragraph text")
        val blocks = listOf(divider, paragraph)

        val result = TextBlockOperations.mergeOnBackspace(blocks, "p1", cursorIndex = 0, now = 2000L)

        assertEquals(1, result.blocks.size)
        val remaining = result.blocks[0] as Block.Paragraph
        assertEquals("p1", remaining.id)
        assertEquals("Paragraph text", remaining.text)
        assertEquals("p1", result.focusBlockId)
        assertEquals(0, result.focusCursorIndex)
    }

    @Test
    fun pasteMultiLineIntoBullet_splitsIntoBulletsFilteringBlankLines() {
        val bullet = Block.BulletItem(id = "b1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val pasted = "One\n\n  \nTwo\nThree"

        val result = TextBlockOperations.paste(listOf(bullet), "b1", pastedText = pasted, cursorIndex = 0, now = 2000L)

        assertEquals(3, result.blocks.size)
        assertTrue(result.blocks.all { it is Block.BulletItem })
        assertEquals("One", (result.blocks[0] as Block.BulletItem).text)
        assertEquals("Two", (result.blocks[1] as Block.BulletItem).text)
        assertEquals("Three", (result.blocks[2] as Block.BulletItem).text)
    }

    @Test
    fun pasteMultiLineIntoNumbered_splitsIntoNumberedItemsFilteringBlankLines() {
        val numbered = Block.NumberedItem(id = "n1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val pasted = "Alpha\n\nBeta\nGamma"

        val result = TextBlockOperations.paste(listOf(numbered), "n1", pastedText = pasted, cursorIndex = 0, now = 2000L)

        assertEquals(3, result.blocks.size)
        assertTrue(result.blocks.all { it is Block.NumberedItem })
        assertEquals("Alpha", (result.blocks[0] as Block.NumberedItem).text)
        assertEquals("Beta", (result.blocks[1] as Block.NumberedItem).text)
        assertEquals("Gamma", (result.blocks[2] as Block.NumberedItem).text)
    }

    @Test
    fun convertTo_allTextLikeTypes_preservesTextExactlyForSingleLine() {
        val textWithSpaces = "  Hello world  "
        val paragraph = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = textWithSpaces)

        val heading = TextBlockOperations.convertTo(paragraph, Block.Heading::class)[0] as Block.Heading
        assertEquals(textWithSpaces, heading.text)

        val checklist = TextBlockOperations.convertTo(heading, Block.Checklist::class)[0] as Block.Checklist
        assertEquals(textWithSpaces, checklist.text)

        val bullet = TextBlockOperations.convertTo(checklist, Block.BulletItem::class)[0] as Block.BulletItem
        assertEquals(textWithSpaces, bullet.text)

        val numbered = TextBlockOperations.convertTo(bullet, Block.NumberedItem::class)[0] as Block.NumberedItem
        assertEquals(textWithSpaces, numbered.text)

        val quote = TextBlockOperations.convertTo(numbered, Block.Quote::class)[0] as Block.Quote
        assertEquals(textWithSpaces, quote.text)

        val backToParagraph = TextBlockOperations.convertTo(quote, Block.Paragraph::class)[0] as Block.Paragraph
        assertEquals(textWithSpaces, backToParagraph.text)
    }

    @Test
    fun numberFor_computesConsecutiveNumberedItemIndicesCorrectly() {
        val n1 = Block.NumberedItem(id = "n1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "1")
        val n2 = Block.NumberedItem(id = "n2", position = 1, createdAt = 1000L, updatedAt = 1000L, text = "2")
        val n3 = Block.NumberedItem(id = "n3", position = 2, createdAt = 1000L, updatedAt = 1000L, text = "3")

        val threeInARow = listOf(n1, n2, n3)
        assertEquals(1, TextBlockOperations.numberFor(threeInARow, 0))
        assertEquals(2, TextBlockOperations.numberFor(threeInARow, 1))
        assertEquals(3, TextBlockOperations.numberFor(threeInARow, 2))

        val p = Block.Paragraph(id = "p1", position = 1, createdAt = 1000L, updatedAt = 1000L, text = "Para")
        val withParagraph = listOf(n1, p, n2, n3)
        assertEquals(1, TextBlockOperations.numberFor(withParagraph, 0))
        assertEquals(0, TextBlockOperations.numberFor(withParagraph, 1))
        assertEquals(1, TextBlockOperations.numberFor(withParagraph, 2))
        assertEquals(2, TextBlockOperations.numberFor(withParagraph, 3))

        val deletedMiddle = listOf(n1, n3)
        assertEquals(1, TextBlockOperations.numberFor(deletedMiddle, 0))
        assertEquals(2, TextBlockOperations.numberFor(deletedMiddle, 1))

        val reordered = listOf(p, n1, n2)
        assertEquals(0, TextBlockOperations.numberFor(reordered, 0))
        assertEquals(1, TextBlockOperations.numberFor(reordered, 1))
        assertEquals(2, TextBlockOperations.numberFor(reordered, 2))
    }

    @Test
    fun insertBlockBelow_insertsOrConvertsEmptyParagraph() {
        val p1 = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Non empty")
        val resultInsert = TextBlockOperations.insertBlockBelow(listOf(p1), "p1", Block.Quote::class, now = 2000L)

        assertEquals(2, resultInsert.blocks.size)
        assertEquals("p1", resultInsert.blocks[0].id)
        assertTrue(resultInsert.blocks[1] is Block.Quote)
        assertEquals(resultInsert.blocks[1].id, resultInsert.focusBlockId)

        val emptyP = Block.Paragraph(id = "ep", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val resultConvert = TextBlockOperations.insertBlockBelow(listOf(emptyP), "ep", Block.BulletItem::class, now = 2000L)

        assertEquals(1, resultConvert.blocks.size)
        assertTrue(resultConvert.blocks[0] is Block.BulletItem)
        assertEquals("ep", resultConvert.blocks[0].id)
    }

    @Test
    fun insertDivider_addsDividerAndEmptyParagraph() {
        val p1 = Block.Paragraph(id = "p1", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "Above")
        val resultInsert = TextBlockOperations.insertDivider(listOf(p1), "p1", now = 2000L)

        assertEquals(3, resultInsert.blocks.size)
        assertTrue(resultInsert.blocks[0] is Block.Paragraph)
        assertTrue(resultInsert.blocks[1] is Block.Divider)
        assertTrue(resultInsert.blocks[2] is Block.Paragraph)
        assertEquals(resultInsert.blocks[2].id, resultInsert.focusBlockId)

        val emptyP = Block.Paragraph(id = "ep", position = 0, createdAt = 1000L, updatedAt = 1000L, text = "")
        val resultConvert = TextBlockOperations.insertDivider(listOf(emptyP), "ep", now = 2000L)

        assertEquals(2, resultConvert.blocks.size)
        assertTrue(resultConvert.blocks[0] is Block.Divider)
        assertTrue(resultConvert.blocks[1] is Block.Paragraph)
        assertEquals(resultConvert.blocks[1].id, resultConvert.focusBlockId)
    }

    @Test
    fun dividerCanNeverBeLastBlock() {
        val divider = Block.Divider(id = "d1", position = 0, createdAt = 1000L, updatedAt = 1000L)
        val ensured = TextBlockOperations.ensureNonEmptyBlocks(listOf(divider), now = 2000L)

        assertEquals(2, ensured.size)
        assertTrue(ensured[0] is Block.Divider)
        assertTrue(ensured[1] is Block.Paragraph)
    }

    @Test
    fun insertBlockBelow_whenTargetIsDivider_insertsBlockBelowDivider() {
        val divider = Block.Divider(id = "d1", position = 0, createdAt = 1000L, updatedAt = 1000L)
        val pAfter = Block.Paragraph(id = "p1", position = 1, createdAt = 1000L, updatedAt = 1000L, text = "End")
        val blocks = listOf(divider, pAfter)

        val result = TextBlockOperations.insertBlockBelow(blocks, "d1", Block.Quote::class, now = 2000L)

        assertEquals(3, result.blocks.size)
        assertTrue(result.blocks[0] is Block.Divider)
        assertTrue(result.blocks[1] is Block.Quote)
        assertTrue(result.blocks[2] is Block.Paragraph)
        assertEquals(result.blocks[1].id, result.focusBlockId)
    }

    @Test
    fun insertBlockBelow_belowCheckedChecklist_createsUncheckedChecklist() {
        val checkedChecklist = Block.Checklist(
            id = "c1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = "Checked item",
            isChecked = true
        )
        val result = TextBlockOperations.insertBlockBelow(
            listOf(checkedChecklist),
            "c1",
            Block.Checklist::class,
            now = 2000L
        )

        assertEquals(2, result.blocks.size)
        val inserted = result.blocks[1] as Block.Checklist
        assertEquals(false, inserted.isChecked)
    }

    @Test
    fun insertBlockBelow_insertedBlockCreatedAtEqualsNow() {
        val targetBlock = Block.Checklist(
            id = "c1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = "Checked item",
            isChecked = true
        )
        val now = 2000L
        val result = TextBlockOperations.insertBlockBelow(
            listOf(targetBlock),
            "c1",
            Block.Checklist::class,
            now = now
        )

        assertEquals(2, result.blocks.size)
        val inserted = result.blocks[1] as Block.Checklist
        assertEquals(now, inserted.createdAt)
        assertTrue(inserted.createdAt != targetBlock.createdAt)
    }

    @Test
    fun convertTo_checklistToChecklist_preservesIsChecked() {
        val checkedChecklist = Block.Checklist(
            id = "c1",
            position = 0,
            createdAt = 1000L,
            updatedAt = 1000L,
            text = "Checked item",
            isChecked = true
        )
        val converted = TextBlockOperations.convertTo(
            checkedChecklist,
            Block.Checklist::class,
            now = 2000L
        )

        assertEquals(1, converted.size)
        val resultBlock = converted[0] as Block.Checklist
        assertEquals(true, resultBlock.isChecked)
        assertEquals(1000L, resultBlock.createdAt)
    }
}
