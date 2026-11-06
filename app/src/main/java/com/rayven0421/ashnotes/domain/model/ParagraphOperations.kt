package com.rayven0421.ashnotes.domain.model

import java.util.UUID

object ParagraphOperations {

    fun splitParagraphBlock(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex]
        val safeText = when (targetBlock) {
            is Block.Paragraph -> targetBlock.text
            is Block.Heading -> targetBlock.text
            else -> return OperationResult(blocks, null)
        }

        val safeCursor = cursorIndex.coerceIn(0, safeText.length)
        val leftText = safeText.substring(0, safeCursor)
        val rightText = safeText.substring(safeCursor)

        val updatedTarget = when (targetBlock) {
            is Block.Paragraph -> targetBlock.copy(text = leftText, updatedAt = now)
            is Block.Heading -> targetBlock.copy(text = leftText, updatedAt = now)
            else -> targetBlock
        }

        val newBlock = Block.Paragraph(
            id = UUID.randomUUID().toString(),
            position = targetBlock.position + 1,
            createdAt = now,
            updatedAt = now,
            metadata = targetBlock.metadata,
            text = rightText
        )

        val updated = blocks.toMutableList().apply {
            set(targetIndex, updatedTarget)
            add(targetIndex + 1, newBlock)
        }
        return OperationResult(repositionBlocks(updated), newBlock.id, 0)
    }

    fun splitHeadingBlock(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        return splitParagraphBlock(blocks, targetBlockId, cursorIndex, now)
    }

    fun mergeOnBackspace(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        if (cursorIndex != 0) return OperationResult(blocks, null)

        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex <= 0) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex]
        val targetText = when (targetBlock) {
            is Block.Paragraph -> targetBlock.text
            is Block.Heading -> targetBlock.text
            is Block.Checklist -> targetBlock.text
            else -> return OperationResult(blocks, null)
        }

        val prevBlock = blocks[targetIndex - 1]
        val prevText = when (prevBlock) {
            is Block.Paragraph -> prevBlock.text
            is Block.Heading -> prevBlock.text
            is Block.Checklist -> prevBlock.text
            else -> return OperationResult(blocks, null)
        }

        val prevLength = prevText.length
        val mergedText = prevText + targetText

        val updatedPrev = when (prevBlock) {
            is Block.Paragraph -> prevBlock.copy(text = mergedText, updatedAt = now)
            is Block.Heading -> prevBlock.copy(text = mergedText, updatedAt = now)
            is Block.Checklist -> prevBlock.copy(text = mergedText, updatedAt = now)
            else -> prevBlock
        }

        val updated = blocks.toMutableList().apply {
            set(targetIndex - 1, updatedPrev)
            removeAt(targetIndex)
        }
        return OperationResult(repositionBlocks(updated), prevBlock.id, prevLength)
    }

    fun pasteTextIntoParagraph(
        blocks: List<Block>,
        targetBlockId: String,
        pastedText: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex]
        if (targetBlock !is Block.Paragraph) return OperationResult(blocks, null)

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

        val middleLines = lines.subList(1, lines.size - 1)
        val lastLineText = lines.last() + suffix

        val newBlocks = mutableListOf<Block>()
        newBlocks.add(updatedFirst)

        middleLines.forEach { line ->
            newBlocks.add(
                Block.Paragraph(
                    id = UUID.randomUUID().toString(),
                    position = 0,
                    createdAt = now,
                    updatedAt = now,
                    metadata = targetBlock.metadata,
                    text = line
                )
            )
        }

        val lastBlock = Block.Paragraph(
            id = UUID.randomUUID().toString(),
            position = 0,
            createdAt = now,
            updatedAt = now,
            metadata = targetBlock.metadata,
            text = lastLineText
        )
        newBlocks.add(lastBlock)

        val updatedList = blocks.toMutableList().apply {
            removeAt(targetIndex)
            addAll(targetIndex, newBlocks)
        }

        return OperationResult(repositionBlocks(updatedList), lastBlock.id, lines.last().length)
    }

    fun pasteTextIntoHeading(
        blocks: List<Block>,
        targetBlockId: String,
        pastedText: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult {
        val targetIndex = blocks.indexOfFirst { it.id == targetBlockId }
        if (targetIndex == -1) return OperationResult(blocks, null)

        val targetBlock = blocks[targetIndex]
        if (targetBlock !is Block.Heading) return OperationResult(blocks, null)

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

        val middleLines = lines.subList(1, lines.size - 1)
        val lastLineText = lines.last() + suffix

        val newBlocks = mutableListOf<Block>()
        newBlocks.add(updatedFirst)

        middleLines.forEach { line ->
            newBlocks.add(
                Block.Paragraph(
                    id = UUID.randomUUID().toString(),
                    position = 0,
                    createdAt = now,
                    updatedAt = now,
                    metadata = targetBlock.metadata,
                    text = line
                )
            )
        }

        val lastBlock = Block.Paragraph(
            id = UUID.randomUUID().toString(),
            position = 0,
            createdAt = now,
            updatedAt = now,
            metadata = targetBlock.metadata,
            text = lastLineText
        )
        newBlocks.add(lastBlock)

        val updatedList = blocks.toMutableList().apply {
            removeAt(targetIndex)
            addAll(targetIndex, newBlocks)
        }

        return OperationResult(repositionBlocks(updatedList), lastBlock.id, lines.last().length)
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
