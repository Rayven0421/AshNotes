package com.rayven0421.ashnotes.domain.model

object ParagraphOperations {

    fun splitParagraphBlock(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult = TextBlockOperations.split(blocks, targetBlockId, cursorIndex, now)

    fun splitHeadingBlock(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult = TextBlockOperations.split(blocks, targetBlockId, cursorIndex, now)

    fun mergeOnBackspace(
        blocks: List<Block>,
        targetBlockId: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult = TextBlockOperations.mergeOnBackspace(blocks, targetBlockId, cursorIndex, now)

    fun pasteTextIntoParagraph(
        blocks: List<Block>,
        targetBlockId: String,
        pastedText: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult = TextBlockOperations.paste(blocks, targetBlockId, pastedText, cursorIndex, now)

    fun pasteTextIntoHeading(
        blocks: List<Block>,
        targetBlockId: String,
        pastedText: String,
        cursorIndex: Int,
        now: Long = System.currentTimeMillis()
    ): OperationResult = TextBlockOperations.paste(blocks, targetBlockId, pastedText, cursorIndex, now)
}
