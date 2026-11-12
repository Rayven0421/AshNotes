package com.rayven0421.ashnotes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSyncDecisionTest {

    @Test
    fun fastTyping_incomingTextEqualsLastEmittedText_doesNotResync() {
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "ab",
            currentCaret = 2,
            incomingBlockText = "ab",
            recentEmittedTexts = listOf("ab"),
            focusRequest = null
        )

        assertFalse("Should not resync when incoming text matches recentEmittedTexts", decision.shouldResync)
        assertEquals("ab", decision.targetText)
        assertEquals(2, decision.targetCaret)
    }

    @Test
    fun undo_replacingText_resyncsWithClampedCaret() {
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "abc",
            currentCaret = 3,
            incomingBlockText = "hello",
            recentEmittedTexts = listOf("abc"),
            focusRequest = null
        )

        assertTrue("Should resync on undo when incoming text differs from local and recentEmittedTexts", decision.shouldResync)
        assertEquals("hello", decision.targetText)
        assertEquals(3, decision.targetCaret)
    }

    @Test
    fun merge_replacingText_resyncsWithPreservedCaret() {
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "Buy ",
            currentCaret = 4,
            incomingBlockText = "Buy milk",
            recentEmittedTexts = listOf("Buy "),
            focusRequest = null
        )

        assertTrue("Should resync on merge", decision.shouldResync)
        assertEquals("Buy milk", decision.targetText)
        assertEquals(4, decision.targetCaret)
    }

    @Test
    fun focusRequest_overridesResyncDecisionAndSetsTargetCursor() {
        val focusRequest = FocusRequest(blockId = "b1", cursor = 2, token = 1L)
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "",
            currentCaret = 0,
            incomingBlockText = "world",
            recentEmittedTexts = emptyList(),
            focusRequest = focusRequest
        )

        assertTrue("Should resync when FocusRequest is present", decision.shouldResync)
        assertEquals("world", decision.targetText)
        assertEquals(2, decision.targetCaret)
    }

    @Test
    fun staleEcho_recentlyEmittedText_doesNotResyncAndDropsOlderEntries() {
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "a",
            currentCaret = 1,
            incomingBlockText = "ab",
            recentEmittedTexts = listOf("ab", "a"),
            focusRequest = null
        )

        assertFalse("Stale echo matching recentEmittedTexts should not resync", decision.shouldResync)
        assertEquals("a", decision.targetText)
        assertFalse(decision.updatedRecentEmittedTexts.contains("ab"))
    }

    @Test
    fun genuineExternalChange_noMatchingEntry_stillResyncs() {
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "hello",
            currentCaret = 5,
            incomingBlockText = "external update",
            recentEmittedTexts = listOf("hello"),
            focusRequest = null
        )

        assertTrue("Genuine external change should resync", decision.shouldResync)
        assertEquals("external update", decision.targetText)
    }

    @Test
    fun focusRequest_withNewToken_stillResyncsAndSetsCaret() {
        val focusRequest = FocusRequest(blockId = "b1", cursor = 3, token = 99L)
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "hello",
            currentCaret = 5,
            incomingBlockText = "hello",
            recentEmittedTexts = listOf("hello"),
            focusRequest = focusRequest
        )

        assertTrue("FocusRequest with new token should resync and set caret", decision.shouldResync)
        assertEquals(3, decision.targetCaret)
    }
}
