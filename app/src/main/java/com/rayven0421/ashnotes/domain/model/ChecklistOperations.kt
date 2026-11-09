package com.rayven0421.ashnotes.domain.model

data class OperationResult(
    val blocks: List<Block>,
    val focusBlockId: String?,
    val focusCursorIndex: Int = 0
)

object ChecklistOperations {

    fun convertBlockToChecklist(block: Block, now: Long = System.currentTimeMillis()): List<Block> {
        return when (block) {
            is Block.Checklist -> TextBlockOperations.convertTo(block, Block.Paragraph::class, now)
            is TextBlock -> TextBlockOperations.convertTo(block, Block.Checklist::class, now)
            else -> listOf(block)
        }
    }

    fun mapCaretForBlockConversion(
        rawText: String,
        convertedBlocks: List<Block>,
        cursorIndex: Int
    ): FocusRequest? = TextBlockOperations.mapCaretForBlockConversion(rawText, convertedBlocks, cursorIndex)

    fun splitChecklistBlock(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult = TextBlockOperations.split(blocks, targetBlockId, cursorIndex, now)

    fun mergeOrConvertOnBackspace(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult = TextBlockOperations.mergeOnBackspace(blocks, targetBlockId, cursorIndex, now)

    fun pasteTextIntoChecklist(
        blocks: List<Block>,
        targetBlockId: String,
        pastedText: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult = TextBlockOperations.paste(blocks, targetBlockId, pastedText, cursorIndex, now)

    fun ensureNonEmptyBlocks(blocks: List<Block>, now: Long = System.currentTimeMillis()): List<Block> =
        TextBlockOperations.ensureNonEmptyBlocks(blocks, now)
}
