package com.rayven0421.ashnotes.domain.model

data class TextSyncDecision(
    val shouldResync: Boolean,
    val targetText: String,
    val targetCaret: Int,
    val updatedRecentEmittedTexts: List<String>
)

object TextSyncEngine {
    fun computeTextSync(
        currentLocalText: String,
        currentCaret: Int,
        incomingBlockText: String,
        recentEmittedTexts: List<String>,
        focusRequest: FocusRequest?
    ): TextSyncDecision {
        if (focusRequest != null) {
            val targetCaret = focusRequest.cursor.coerceIn(0, incomingBlockText.length)
            return TextSyncDecision(
                shouldResync = true,
                targetText = incomingBlockText,
                targetCaret = targetCaret,
                updatedRecentEmittedTexts = recentEmittedTexts
            )
        }

        if (incomingBlockText == currentLocalText) {
            return TextSyncDecision(
                shouldResync = false,
                targetText = currentLocalText,
                targetCaret = currentCaret,
                updatedRecentEmittedTexts = recentEmittedTexts
            )
        }

        val matchIndex = recentEmittedTexts.indexOf(incomingBlockText)
        if (matchIndex != -1) {
            val updatedList = recentEmittedTexts.drop(matchIndex + 1)
            return TextSyncDecision(
                shouldResync = false,
                targetText = currentLocalText,
                targetCaret = currentCaret,
                updatedRecentEmittedTexts = updatedList
            )
        }

        val targetCaret = minOf(currentCaret, incomingBlockText.length)
        return TextSyncDecision(
            shouldResync = true,
            targetText = incomingBlockText,
            targetCaret = targetCaret,
            updatedRecentEmittedTexts = recentEmittedTexts
        )
    }
}
