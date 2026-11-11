package com.rayven0421.ashnotes.domain.model

import java.util.UUID
import kotlin.reflect.KClass

data class TextBlockRule(
    val create: (source: TextBlock, id: String, position: Int, text: String, now: Long) -> TextBlock,
    val createNextOnSplit: (parent: TextBlock, text: String, now: Long) -> TextBlock,
    val onEmptyAction: ((target: TextBlock, now: Long) -> TextBlock)? = null,
    val createMiddleBlockOnPaste: (parent: TextBlock, text: String, now: Long) -> TextBlock,
    val createLastBlockOnPaste: (parent: TextBlock, text: String, now: Long) -> TextBlock = createMiddleBlockOnPaste,
    val filterBlankMiddleLinesOnPaste: Boolean = false
)

object TextBlockOperations {

    private val paragraphRule = TextBlockRule(
        create = { source, id, position, text, now ->
            Block.Paragraph(
                id = id,
                position = position,
                createdAt = source.createdAt,
                updatedAt = now,
                metadata = source.metadata,
                text = text,
                spans = emptyList()
            )
        },
        createNextOnSplit = { parent, text, now ->
            Block.Paragraph(
                id = UUID.randomUUID().toString(),
                position = parent.position + 1,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        onEmptyAction = null,
        createMiddleBlockOnPaste = { parent, text, now ->
            Block.Paragraph(
                id = UUID.randomUUID().toString(),
                position = 0,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        filterBlankMiddleLinesOnPaste = false
    )

    private val headingRule = TextBlockRule(
        create = { source, id, position, text, now ->
            Block.Heading(
                id = id,
                position = position,
                createdAt = source.createdAt,
                updatedAt = now,
                metadata = source.metadata,
                text = text,
                level = (source as? Block.Heading)?.level ?: 1,
                spans = emptyList()
            )
        },
        createNextOnSplit = { parent, text, now ->
            Block.Paragraph(
                id = UUID.randomUUID().toString(),
                position = parent.position + 1,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        onEmptyAction = null,
        createMiddleBlockOnPaste = { parent, text, now ->
            Block.Paragraph(
                id = UUID.randomUUID().toString(),
                position = 0,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        filterBlankMiddleLinesOnPaste = false
    )

    private val checklistRule = TextBlockRule(
        create = { source, id, position, text, now ->
            Block.Checklist(
                id = id,
                position = position,
                createdAt = source.createdAt,
                updatedAt = now,
                metadata = source.metadata,
                text = text,
                isChecked = (source as? Block.Checklist)?.isChecked ?: false,
                items = emptyList(),
                spans = emptyList()
            )
        },
        createNextOnSplit = { parent, text, now ->
            Block.Checklist(
                id = UUID.randomUUID().toString(),
                position = parent.position + 1,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text,
                isChecked = false
            )
        },
        onEmptyAction = { target, now ->
            Block.Paragraph(
                id = target.id,
                position = target.position,
                createdAt = target.createdAt,
                updatedAt = now,
                metadata = target.metadata,
                text = ""
            )
        },
        createMiddleBlockOnPaste = { parent, text, now ->
            Block.Checklist(
                id = UUID.randomUUID().toString(),
                position = 0,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text,
                isChecked = false
            )
        },
        filterBlankMiddleLinesOnPaste = true
    )

    private val bulletRule = TextBlockRule(
        create = { source, id, position, text, now ->
            Block.BulletItem(
                id = id,
                position = position,
                createdAt = source.createdAt,
                updatedAt = now,
                metadata = source.metadata,
                text = text,
                spans = emptyList()
            )
        },
        createNextOnSplit = { parent, text, now ->
            Block.BulletItem(
                id = UUID.randomUUID().toString(),
                position = parent.position + 1,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        onEmptyAction = { target, now ->
            Block.Paragraph(
                id = target.id,
                position = target.position,
                createdAt = target.createdAt,
                updatedAt = now,
                metadata = target.metadata,
                text = ""
            )
        },
        createMiddleBlockOnPaste = { parent, text, now ->
            Block.BulletItem(
                id = UUID.randomUUID().toString(),
                position = 0,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        filterBlankMiddleLinesOnPaste = true
    )

    private val numberedRule = TextBlockRule(
        create = { source, id, position, text, now ->
            Block.NumberedItem(
                id = id,
                position = position,
                createdAt = source.createdAt,
                updatedAt = now,
                metadata = source.metadata,
                text = text,
                spans = emptyList()
            )
        },
        createNextOnSplit = { parent, text, now ->
            Block.NumberedItem(
                id = UUID.randomUUID().toString(),
                position = parent.position + 1,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        onEmptyAction = { target, now ->
            Block.Paragraph(
                id = target.id,
                position = target.position,
                createdAt = target.createdAt,
                updatedAt = now,
                metadata = target.metadata,
                text = ""
            )
        },
        createMiddleBlockOnPaste = { parent, text, now ->
            Block.NumberedItem(
                id = UUID.randomUUID().toString(),
                position = 0,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        filterBlankMiddleLinesOnPaste = true
    )

    private val quoteRule = TextBlockRule(
        create = { source, id, position, text, now ->
            Block.Quote(
                id = id,
                position = position,
                createdAt = source.createdAt,
                updatedAt = now,
                metadata = source.metadata,
                text = text,
                spans = emptyList()
            )
        },
        createNextOnSplit = { parent, text, now ->
            Block.Quote(
                id = UUID.randomUUID().toString(),
                position = parent.position + 1,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        onEmptyAction = { target, now ->
            Block.Paragraph(
                id = target.id,
                position = target.position,
                createdAt = target.createdAt,
                updatedAt = now,
                metadata = target.metadata,
                text = ""
            )
        },
        createMiddleBlockOnPaste = { parent, text, now ->
            Block.Quote(
                id = UUID.randomUUID().toString(),
                position = 0,
                createdAt = now,
                updatedAt = now,
                metadata = parent.metadata,
                text = text
            )
        },
        filterBlankMiddleLinesOnPaste = false
    )

    fun ruleFor(block: TextBlock): TextBlockRule = when (block) {
        is Block.Paragraph -> paragraphRule
        is Block.Heading -> headingRule
        is Block.Checklist -> checklistRule
        is Block.BulletItem -> bulletRule
        is Block.NumberedItem -> numberedRule
        is Block.Quote -> quoteRule
    }

    fun ruleForType(type: KClass<out TextBlock>): TextBlockRule = when (type) {
        Block.Paragraph::class -> paragraphRule
        Block.Heading::class -> headingRule
        Block.Checklist::class -> checklistRule
        Block.BulletItem::class -> bulletRule
        Block.NumberedItem::class -> numberedRule
        Block.Quote::class -> quoteRule
        else -> error("Unhandled TextBlock type: $type")
    }

    fun numberFor(blocks: List<Block>, index: Int): Int {
        if (index !in blocks.indices) return 0
        if (blocks[index] !is Block.NumberedItem) return 0
        var count = 1
        var i = index - 1
        while (i >= 0 && blocks[i] is Block.NumberedItem) {
            count++
            i--
        }
        return count
    }

    fun split(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex] as? TextBlock ?: return OperationResult(blocks, null)
        val rule = ruleFor(targetBlock)

        if (targetBlock.text.isEmpty() && rule.onEmptyAction != null) {
            val converted = rule.onEmptyAction.invoke(targetBlock, now)
            val updated = blocks.toMutableList().apply { set(targetIndex, converted) }
            val ensured = ensureNonEmptyBlocks(updated, now)
            return OperationResult(ensured, converted.id, 0)
        }

        val safeCursor = cursorIndex.coerceIn(0, targetBlock.text.length)
        val leftText = targetBlock.text.substring(0, safeCursor)
        val rightText = targetBlock.text.substring(safeCursor)

        val updatedTarget = targetBlock.withText(leftText, now)
        val newBlock = rule.createNextOnSplit(targetBlock, rightText, now)

        val updated = blocks.toMutableList().apply {
            set(targetIndex, updatedTarget)
            add(targetIndex + 1, newBlock)
        }
        val ensured = ensureNonEmptyBlocks(updated, now)
        return OperationResult(ensured, newBlock.id, 0)
    }

    fun mergeOnBackspace(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        if (cursorIndex != 0) return OperationResult(blocks, null)

        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex] as? TextBlock ?: return OperationResult(blocks, null)
        val rule = ruleFor(targetBlock)

        if (targetBlock.text.isEmpty() && rule.onEmptyAction != null) {
            val converted = rule.onEmptyAction.invoke(targetBlock, now)
            val updated = blocks.toMutableList().apply { set(targetIndex, converted) }
            val ensured = ensureNonEmptyBlocks(updated, now)
            return OperationResult(ensured, converted.id, 0)
        }

        if (targetIndex <= 0) return OperationResult(blocks, null)

        val prevBlock = blocks[targetIndex - 1]
        if (prevBlock is Block.Divider) {
            val updated = blocks.toMutableList().apply { removeAt(targetIndex - 1) }
            val ensured = ensureNonEmptyBlocks(updated, now)
            return OperationResult(ensured, targetBlock.id, 0)
        }

        val prevTextBlock = prevBlock as? TextBlock ?: return OperationResult(blocks, null)

        val prevLength = prevTextBlock.text.length
        val mergedText = prevTextBlock.text + targetBlock.text

        val updatedPrev = prevTextBlock.withText(mergedText, now)

        val updated = blocks.toMutableList().apply {
            set(targetIndex - 1, updatedPrev)
            removeAt(targetIndex)
        }
        val ensured = ensureNonEmptyBlocks(updated, now)
        return OperationResult(ensured, prevTextBlock.id, prevLength)
    }

    fun paste(
        blocks: List<Block>,
        targetBlockId: String,
        pastedText: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex] as? TextBlock ?: return OperationResult(blocks, null)
        val rule = ruleFor(targetBlock)

        val lines = pastedText.split("\n")
        if (lines.size <= 1) {
            val safeCursor = cursorIndex.coerceIn(0, targetBlock.text.length)
            val newText = targetBlock.text.substring(0, safeCursor) + pastedText + targetBlock.text.substring(safeCursor)
            val updated = targetBlock.withText(newText, now)
            val updatedList = blocks.toMutableList().apply { set(targetIndex, updated) }
            val ensured = ensureNonEmptyBlocks(updatedList, now)
            return OperationResult(ensured, targetBlock.id, safeCursor + pastedText.length)
        }

        val safeCursor = cursorIndex.coerceIn(0, targetBlock.text.length)
        val prefix = targetBlock.text.substring(0, safeCursor)
        val suffix = targetBlock.text.substring(safeCursor)

        val firstLineText = prefix + lines.first()
        val updatedFirst = targetBlock.withText(firstLineText, now)

        var rawMiddleLines = lines.subList(1, lines.size - 1)
        if (rule.filterBlankMiddleLinesOnPaste) {
            rawMiddleLines = rawMiddleLines.filter { it.isNotBlank() }
        }
        val lastLineText = lines.last() + suffix

        val newBlocks = mutableListOf<Block>()
        newBlocks.add(updatedFirst)

        rawMiddleLines.forEach { line ->
            newBlocks.add(rule.createMiddleBlockOnPaste(targetBlock, line, now))
        }

        val lastBlock = rule.createLastBlockOnPaste(targetBlock, lastLineText, now)
        newBlocks.add(lastBlock)

        val updatedList = blocks.toMutableList().apply {
            removeAt(targetIndex)
            addAll(targetIndex, newBlocks)
        }

        val ensured = ensureNonEmptyBlocks(updatedList, now)
        return OperationResult(ensured, lastBlock.id, lines.last().length)
    }

    fun insertBlockBelow(
        blocks: List<Block>,
        afterBlockId: String,
        type: KClass<out TextBlock>,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == afterBlockId }
        if (targetIndex == -1) {
            val dummySource = Block.Paragraph(id = "", position = 0, createdAt = now, updatedAt = now)
            val newBlock = ruleForType(type).create(
                dummySource,
                UUID.randomUUID().toString(),
                blocks.size,
                "",
                now
            )
            val updated = blocks + newBlock
            val ensured = ensureNonEmptyBlocks(updated, now)
            return OperationResult(ensured, newBlock.id, 0)
        }

        val targetBlock = blocks[targetIndex]
        if (targetBlock is Block.Paragraph && targetBlock.text.isEmpty()) {
            val converted = ruleForType(type).create(targetBlock, targetBlock.id, targetBlock.position, "", now)
            val updated = blocks.toMutableList().apply { set(targetIndex, converted) }
            val ensured = ensureNonEmptyBlocks(updated, now)
            return OperationResult(ensured, converted.id, 0)
        }

        val dummySource = Block.Paragraph(
            id = "",
            position = 0,
            createdAt = now,
            updatedAt = now,
            metadata = (targetBlock as? TextBlock)?.metadata ?: emptyMap()
        )
        val newBlock = ruleForType(type).create(
            dummySource,
            UUID.randomUUID().toString(),
            targetIndex + 1,
            "",
            now
        )
        val updated = blocks.toMutableList().apply { add(targetIndex + 1, newBlock) }
        val ensured = ensureNonEmptyBlocks(updated, now)
        return OperationResult(ensured, newBlock.id, 0)
    }

    fun insertDivider(
        blocks: List<Block>,
        afterBlockId: String,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == afterBlockId }
        val dividerId = UUID.randomUUID().toString()
        val paragraphAfterId = UUID.randomUUID().toString()

        if (targetIndex == -1) {
            val divider = Block.Divider(id = dividerId, position = blocks.size, createdAt = now, updatedAt = now)
            val paragraph = Block.Paragraph(id = paragraphAfterId, position = blocks.size + 1, createdAt = now, updatedAt = now, text = "")
            val updated = blocks + divider + paragraph
            val ensured = ensureNonEmptyBlocks(updated, now)
            return OperationResult(ensured, paragraphAfterId, 0)
        }

        val targetBlock = blocks[targetIndex]
        if (targetBlock is Block.Paragraph && targetBlock.text.isEmpty()) {
            val divider = Block.Divider(id = dividerId, position = targetIndex, createdAt = targetBlock.createdAt, updatedAt = now, metadata = targetBlock.metadata)
            val paragraph = Block.Paragraph(id = paragraphAfterId, position = targetIndex + 1, createdAt = now, updatedAt = now, metadata = targetBlock.metadata, text = "")
            val updated = blocks.toMutableList().apply {
                set(targetIndex, divider)
                add(targetIndex + 1, paragraph)
            }
            val ensured = ensureNonEmptyBlocks(updated, now)
            return OperationResult(ensured, paragraphAfterId, 0)
        }

        val divider = Block.Divider(id = dividerId, position = targetIndex + 1, createdAt = now, updatedAt = now, metadata = targetBlock.metadata)
        val paragraph = Block.Paragraph(id = paragraphAfterId, position = targetIndex + 2, createdAt = now, updatedAt = now, metadata = targetBlock.metadata, text = "")
        val updated = blocks.toMutableList().apply {
            add(targetIndex + 1, divider)
            add(targetIndex + 2, paragraph)
        }
        val ensured = ensureNonEmptyBlocks(updated, now)
        return OperationResult(ensured, paragraphAfterId, 0)
    }

    fun convertTo(
        block: Block,
        targetType: KClass<out TextBlock>,
        now: Long = System.currentTimeMillis()
    ): List<Block> {
        val textBlock = block as? TextBlock ?: return listOf(block)
        val targetRule = ruleForType(targetType)

        val isMultiLineToItem = textBlock.text.contains("\n") && (
            targetType == Block.Checklist::class ||
            targetType == Block.BulletItem::class ||
            targetType == Block.NumberedItem::class
        )

        if (isMultiLineToItem) {
            val lines = textBlock.text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) {
                return listOf(targetRule.create(textBlock, textBlock.id, textBlock.position, "", now))
            }
            return lines.mapIndexed { index, lineText ->
                targetRule.create(
                    textBlock,
                    if (index == 0) textBlock.id else UUID.randomUUID().toString(),
                    textBlock.position + index,
                    lineText,
                    now
                )
            }
        }

        return listOf(targetRule.create(textBlock, textBlock.id, textBlock.position, textBlock.text, now))
    }

    fun mapCaretForBlockConversion(
        rawText: String,
        convertedBlocks: List<Block>,
        cursorIndex: Int
    ): FocusRequest? {
        if (convertedBlocks.isEmpty()) return null
        val rawLines = rawText.split("\n")
        if (convertedBlocks.size > 1 && rawText.contains("\n")) {
            val clampedCursor = cursorIndex.coerceIn(0, rawText.length)
            var accumulatedLen = 0
            var targetLineIndex = 0
            var cursorInLine = clampedCursor

            for (i in rawLines.indices) {
                val lineLen = rawLines[i].length
                val lineEnd = accumulatedLen + lineLen
                if (clampedCursor <= lineEnd || i == rawLines.size - 1) {
                    targetLineIndex = i
                    cursorInLine = (clampedCursor - accumulatedLen).coerceIn(0, lineLen)
                    break
                }
                accumulatedLen += lineLen + 1
            }

            val convertedIndex = rawLines.take(targetLineIndex).count { it.trim().isNotEmpty() }
            val focusBlock = convertedBlocks.getOrNull(convertedIndex)
                ?: convertedBlocks.lastOrNull()
                ?: convertedBlocks.firstOrNull()
                ?: return null

            val textLen = (focusBlock as? TextBlock)?.text?.length ?: 0
            return FocusRequest(blockId = focusBlock.id, cursor = cursorInLine.coerceIn(0, textLen))
        } else {
            val firstBlock = convertedBlocks.firstOrNull() ?: return null
            val textLen = (firstBlock as? TextBlock)?.text?.length ?: 0
            return FocusRequest(blockId = firstBlock.id, cursor = cursorIndex.coerceIn(0, textLen))
        }
    }

    fun convertBlock(
        blocks: List<Block>,
        targetBlockId: String,
        targetType: KClass<out TextBlock>,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): Pair<List<Block>, FocusRequest?> {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return Pair(blocks, null)

        val targetBlock = blocks[targetIndex] as? TextBlock ?: return Pair(blocks, null)
        val rawText = targetBlock.text
        val converted = convertTo(targetBlock, targetType, now)

        val updatedBlocks = blocks.toMutableList().apply {
            removeAt(targetIndex)
            addAll(targetIndex, converted)
        }

        val ensured = ensureNonEmptyBlocks(updatedBlocks, now)
        val focusRequest = mapCaretForBlockConversion(
            rawText = rawText,
            convertedBlocks = converted,
            cursorIndex = cursorIndex
        )

        return Pair(ensured, focusRequest)
    }

    fun toggleChecklistBlock(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): Pair<List<Block>, FocusRequest?> {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return Pair(blocks, null)

        val targetBlock = blocks[targetIndex] as? TextBlock ?: return Pair(blocks, null)
        val targetType = if (targetBlock is Block.Checklist) Block.Paragraph::class else Block.Checklist::class
        return convertBlock(blocks, targetBlockId, targetType, cursorIndex, now)
    }

    fun deleteBlock(
        blocks: List<Block>,
        targetBlockId: String,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val updated = blocks.toMutableList().apply { removeAt(targetIndex) }
        val ensured = ensureNonEmptyBlocks(updated, now)
        val focusIndex = (targetIndex - 1).coerceAtLeast(0).coerceIn(ensured.indices)
        val focusBlock = ensured[focusIndex]
        val focusCursor = (focusBlock as? TextBlock)?.text?.length ?: 0
        return OperationResult(ensured, focusBlock.id, focusCursor)
    }

    fun ensureNonEmptyBlocks(blocks: List<Block>, now: Long = System.currentTimeMillis()): List<Block> {
        if (blocks.isEmpty()) {
            return listOf(
                Block.Paragraph(
                    id = UUID.randomUUID().toString(),
                    position = 0,
                    createdAt = now,
                    updatedAt = now,
                    text = ""
                )
            )
        }
        val mutable = blocks.toMutableList()
        if (mutable.last() is Block.Divider) {
            mutable.add(
                Block.Paragraph(
                    id = UUID.randomUUID().toString(),
                    position = mutable.size,
                    createdAt = now,
                    updatedAt = now,
                    text = ""
                )
            )
        }
        return repositionBlocks(mutable)
    }

    fun repositionBlocks(blocks: List<Block>): List<Block> {
        return blocks.mapIndexed { index, block ->
            when (block) {
                is Block.Paragraph -> block.copy(position = index)
                is Block.Heading -> block.copy(position = index)
                is Block.Checklist -> block.copy(position = index)
                is Block.BulletItem -> block.copy(position = index)
                is Block.NumberedItem -> block.copy(position = index)
                is Block.Quote -> block.copy(position = index)
                is Block.Divider -> block.copy(position = index)
                is Block.UnknownBlock -> block.copy(position = index)
            }
        }
    }
}
