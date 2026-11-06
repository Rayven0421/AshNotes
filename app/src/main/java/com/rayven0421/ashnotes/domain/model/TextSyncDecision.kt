package com.rayven0421.ashnotes.domain.model

data class TextSyncDecision(
    val shouldResync: Boolean,
    val targetText: String,
    val targetCaret: Int
)

object TextSyncEngine {
    fun computeTextSync(
        currentLocalText: String,
        currentCaret: Int,
        incomingBlockText: String,
        lastEmittedText: String?,
        focusRequest: FocusRequest?
    ): TextSyncDecision {
        if (focusRequest != null) {
            val targetCaret = focusRequest.cursor.coerceIn(0, incomingBlockText.length)
            return TextSyncDecision(
                shouldResync = true,
                targetText = incomingBlockText,
                targetCaret = targetCaret
            )
        }

        if (incomingBlockText == currentLocalText || incomingBlockText == lastEmittedText) {
            return TextSyncDecision(
                shouldResync = false,
                targetText = currentLocalText,
                targetCaret = currentCaret
            )
        }

        val targetCaret = minOf(currentCaret, incomingBlockText.length)
        return TextSyncDecision(
            shouldResync = true,
            targetText = incomingBlockText,
            targetCaret = targetCaret
        )
    }
}
