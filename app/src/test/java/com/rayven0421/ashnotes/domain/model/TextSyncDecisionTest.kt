package com.rayven0421.ashnotes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSyncDecisionTest {

    @Test
    fun fastTyping_incomingTextEqualsLastEmittedText_doesNotResync() {
        // User typed 'a', then 'b' quickly.
        // currentLocalText = "ab", lastEmittedText = "ab".
        // State update for "ab" arrives from ViewModel.
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "ab",
            currentCaret = 2,
            incomingBlockText = "ab",
            lastEmittedText = "ab",
            focusRequest = null
        )

        assertFalse("Should not resync when incoming text matches lastEmittedText", decision.shouldResync)
        assertEquals("ab", decision.targetText)
        assertEquals(2, decision.targetCaret)
    }

    @Test
    fun undo_replacingText_resyncsWithClampedCaret() {
        // Local text was "abc", lastEmittedText was "abc".
        // Undo changes block text back to "hello".
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "abc",
            currentCaret = 3,
            incomingBlockText = "hello",
            lastEmittedText = "abc",
            focusRequest = null
        )

        assertTrue("Should resync on undo when incoming text differs from local and lastEmitted", decision.shouldResync)
        assertEquals("hello", decision.targetText)
        assertEquals(3, decision.targetCaret)
    }

    @Test
    fun merge_replacingText_resyncsWithPreservedCaret() {
        // Backspace at end merged next line into current block.
        // Local text was "Buy ", lastEmittedText was "Buy ".
        // Incoming text from merge is "Buy milk".
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "Buy ",
            currentCaret = 4,
            incomingBlockText = "Buy milk",
            lastEmittedText = "Buy ",
            focusRequest = null
        )

        assertTrue("Should resync on merge", decision.shouldResync)
        assertEquals("Buy milk", decision.targetText)
        assertEquals(4, decision.targetCaret)
    }

    @Test
    fun focusRequest_overridesResyncDecisionAndSetsTargetCursor() {
        val focusRequest = FocusRequest(blockId = "b1", cursor = 2)
        val decision = TextSyncEngine.computeTextSync(
            currentLocalText = "",
            currentCaret = 0,
            incomingBlockText = "world",
            lastEmittedText = null,
            focusRequest = focusRequest
        )

        assertTrue("Should resync when FocusRequest is present", decision.shouldResync)
        assertEquals("world", decision.targetText)
        assertEquals(2, decision.targetCaret)
    }
}
