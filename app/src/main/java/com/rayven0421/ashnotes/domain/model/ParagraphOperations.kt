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
