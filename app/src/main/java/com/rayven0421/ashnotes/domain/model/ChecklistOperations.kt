package com.rayven0421.ashnotes.domain.model

import java.util.UUID

data class OperationResult(
    val blocks: List<Block>,
    val focusBlockId: String?,
    val focusCursorIndex: Int = 0
)

object ChecklistOperations {

    fun convertBlockToChecklist(block: Block, now: Long = System.currentTimeMillis()): List<Block> {
        return when (block) {
            is Block.Paragraph -> {
                val lines = block.text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                if (lines.isEmpty()) {
                    listOf(
                        Block.Checklist(
                            id = block.id,
                            position = block.position,
                            createdAt = block.createdAt,
                            updatedAt = now,
                            metadata = block.metadata,
                            text = "",
                            isChecked = false
                        )
                    )
                } else {
                    lines.mapIndexed { index, lineText ->
                        Block.Checklist(
                            id = if (index == 0) block.id else UUID.randomUUID().toString(),
                            position = block.position + index,
                            createdAt = block.createdAt,
                            updatedAt = now,
                            metadata = block.metadata,
                            text = lineText,
                            isChecked = false
                        )
                    }
                }
            }
            is Block.Heading -> {
                val lines = block.text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                if (lines.isEmpty()) {
                    listOf(
                        Block.Checklist(
                            id = block.id,
                            position = block.position,
                            createdAt = block.createdAt,
                            updatedAt = now,
                            metadata = block.metadata,
                            text = "",
                            isChecked = false
                        )
                    )
                } else {
                    lines.mapIndexed { index, lineText ->
                        Block.Checklist(
                            id = if (index == 0) block.id else UUID.randomUUID().toString(),
                            position = block.position + index,
                            createdAt = block.createdAt,
                            updatedAt = now,
                            metadata = block.metadata,
                            text = lineText,
                            isChecked = false
                        )
                    }
                }
            }
            is Block.Checklist -> {
                listOf(
                    Block.Paragraph(
                        id = block.id,
                        position = block.position,
                        createdAt = block.createdAt,
                        updatedAt = now,
                        metadata = block.metadata,
                        text = block.text
                    )
                )
            }
            else -> listOf(block)
        }
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
            val focusBlock = convertedBlocks.getOrNull(convertedIndex) ?: convertedBlocks.lastOrNull() ?: convertedBlocks.firstOrNull() ?: return null

            val textLen = when (focusBlock) {
                is Block.Checklist -> focusBlock.text.length
                is Block.Paragraph -> focusBlock.text.length
                is Block.Heading -> focusBlock.text.length
                else -> 0
            }
            return FocusRequest(blockId = focusBlock.id, cursor = cursorInLine.coerceIn(0, textLen))
        } else {
            val firstBlock = convertedBlocks.firstOrNull() ?: return null
            val textLen = when (firstBlock) {
                is Block.Checklist -> firstBlock.text.length
                is Block.Paragraph -> firstBlock.text.length
                is Block.Heading -> firstBlock.text.length
                else -> 0
            }
            return FocusRequest(blockId = firstBlock.id, cursor = cursorIndex.coerceIn(0, textLen))
        }
    }

    fun splitChecklistBlock(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex]
        if (targetBlock !is Block.Checklist) return OperationResult(blocks, null)

        if (targetBlock.text.isEmpty()) {
            val converted = Block.Paragraph(
                id = targetBlock.id,
                position = targetBlock.position,
                createdAt = targetBlock.createdAt,
                updatedAt = now,
                metadata = targetBlock.metadata,
                text = ""
            )
            val updated = blocks.toMutableList().apply { set(targetIndex, converted) }
            return OperationResult(repositionBlocks(updated), converted.id, 0)
        }

        val safeCursor = cursorIndex.coerceIn(0, targetBlock.text.length)
        val leftText = targetBlock.text.substring(0, safeCursor)
        val rightText = targetBlock.text.substring(safeCursor)

        val updatedTarget = targetBlock.copy(text = leftText, updatedAt = now)
        val newBlock = Block.Checklist(
            id = UUID.randomUUID().toString(),
            position = targetBlock.position + 1,
            createdAt = now,
            updatedAt = now,
            metadata = targetBlock.metadata,
            text = rightText,
            isChecked = false
        )

        val updated = blocks.toMutableList().apply {
            set(targetIndex, updatedTarget)
            add(targetIndex + 1, newBlock)
        }
        return OperationResult(repositionBlocks(updated), newBlock.id, 0)
    }

    fun mergeOrConvertOnBackspace(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        if (cursorIndex != 0) return OperationResult(blocks, null)

        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex]
        if (targetBlock !is Block.Checklist) return OperationResult(blocks, null)

        if (targetBlock.text.isEmpty()) {
            val converted = Block.Paragraph(
                id = targetBlock.id,
                position = targetBlock.position,
                createdAt = targetBlock.createdAt,
                updatedAt = now,
                metadata = targetBlock.metadata,
                text = ""
            )
            val updated = blocks.toMutableList().apply { set(targetIndex, converted) }
            return OperationResult(repositionBlocks(updated), converted.id, 0)
        }

        if (targetIndex == 0) return OperationResult(blocks, null)
        val prevBlock = blocks[targetIndex - 1]

        return when (prevBlock) {
            is Block.Checklist -> {
                val prevLength = prevBlock.text.length
                val mergedPrev = prevBlock.copy(text = prevBlock.text + targetBlock.text, updatedAt = now)
                val updated = blocks.toMutableList().apply {
                    set(targetIndex - 1, mergedPrev)
                    removeAt(targetIndex)
                }
                OperationResult(repositionBlocks(updated), prevBlock.id, prevLength)
            }
            is Block.Paragraph -> {
                val prevLength = prevBlock.text.length
                val mergedPrev = prevBlock.copy(text = prevBlock.text + targetBlock.text, updatedAt = now)
                val updated = blocks.toMutableList().apply {
                    set(targetIndex - 1, mergedPrev)
                    removeAt(targetIndex)
                }
                OperationResult(repositionBlocks(updated), prevBlock.id, prevLength)
            }
            is Block.Heading -> {
                val prevLength = prevBlock.text.length
                val mergedPrev = prevBlock.copy(text = prevBlock.text + targetBlock.text, updatedAt = now)
                val updated = blocks.toMutableList().apply {
                    set(targetIndex - 1, mergedPrev)
                    removeAt(targetIndex)
                }
                OperationResult(repositionBlocks(updated), prevBlock.id, prevLength)
            }
            else -> OperationResult(blocks, null)
        }
    }

    fun pasteTextIntoChecklist(
        blocks: List<Block>,
        targetBlockId: String,
        pastedText: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex]
        if (targetBlock !is Block.Checklist) return OperationResult(blocks, null)

        val lines = pastedText.split("\n")
        if (lines.size <= 1) {
            val safeCursor = cursorIndex.coerceIn(0, targetBlock.text.length)
            val newText = targetBlock.text.substring(0, safeCursor) + pastedText + targetBlock.text.substring(safeCursor)
            val updated = targetBlock.copy(text = newText, updatedAt = now)
            val updatedList = blocks.toMutableList().apply { set(targetIndex, updated) }
            return OperationResult(repositionBlocks(updatedList), targetBlock.id, safeCursor + pastedText.length)
        }

        val safeCursor = cursorIndex.coerceIn(0, targetBlock.text.length)
        val prefix = targetBlock.text.substring(0, safeCursor)
        val suffix = targetBlock.text.substring(safeCursor)

        val firstLineText = prefix + lines.first()
        val updatedFirst = targetBlock.copy(text = firstLineText, updatedAt = now)

        val middleLines = lines.subList(1, lines.size - 1).filter { it.isNotBlank() }
        val lastLineText = lines.last() + suffix

        val newBlocks = mutableListOf<Block>()
        newBlocks.add(updatedFirst)

        middleLines.forEach { line ->
            newBlocks.add(
                Block.Checklist(
                    id = UUID.randomUUID().toString(),
                    position = 0,
                    createdAt = now,
                    updatedAt = now,
                    metadata = targetBlock.metadata,
                    text = line,
                    isChecked = false
                )
            )
        }

        val lastBlock = Block.Checklist(
            id = UUID.randomUUID().toString(),
            position = 0,
            createdAt = now,
            updatedAt = now,
            metadata = targetBlock.metadata,
            text = lastLineText,
            isChecked = false
        )
        newBlocks.add(lastBlock)

        val updatedList = blocks.toMutableList().apply {
            removeAt(targetIndex)
            addAll(targetIndex, newBlocks)
        }

        return OperationResult(repositionBlocks(updatedList), lastBlock.id, lines.last().length)
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
        return repositionBlocks(blocks)
    }

    private fun repositionBlocks(blocks: List<Block>): List<Block> {
        return blocks.mapIndexed { index, block ->
            when (block) {
                is Block.Paragraph -> block.copy(position = index)
                is Block.Heading -> block.copy(position = index)
                is Block.Checklist -> block.copy(position = index)
                is Block.UnknownBlock -> block.copy(position = index)
            }
        }
    }
}
