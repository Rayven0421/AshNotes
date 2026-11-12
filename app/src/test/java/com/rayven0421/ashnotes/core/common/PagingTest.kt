package com.rayven0421.ashnotes.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class PagingTest {

    @Test
    fun paginate_emptyList_returnsSingleEmptyPage() {
        val result = paginate(emptyList<Int>(), page = 1, pageSize = 10)
        assertEquals(emptyList<Int>(), result.items)
        assertEquals(1, result.currentPage)
        assertEquals(1, result.totalPages)
    }

    @Test
    fun paginate_exactly10Items_returnsSinglePageWith10Items() {
        val items = (1..10).toList()
        val result = paginate(items, page = 1, pageSize = 10)
        assertEquals(10, result.items.size)
        assertEquals((1..10).toList(), result.items)
        assertEquals(1, result.currentPage)
        assertEquals(1, result.totalPages)
    }

    @Test
    fun paginate_11Items_returnsTwoPages() {
        val items = (1..11).toList()
        val page1 = paginate(items, page = 1, pageSize = 10)
        assertEquals(10, page1.items.size)
        assertEquals((1..10).toList(), page1.items)
        assertEquals(1, page1.currentPage)
        assertEquals(2, page1.totalPages)

        val page2 = paginate(items, page = 2, pageSize = 10)
        assertEquals(1, page2.items.size)
        assertEquals(listOf(11), page2.items)
        assertEquals(2, page2.currentPage)
        assertEquals(2, page2.totalPages)
    }

    @Test
    fun paginate_25Items_returnsThreePages() {
        val items = (1..25).toList()
        val page3 = paginate(items, page = 3, pageSize = 10)
        assertEquals(5, page3.items.size)
        assertEquals((21..25).toList(), page3.items)
        assertEquals(3, page3.currentPage)
        assertEquals(3, page3.totalPages)
    }

    @Test
    fun paginate_pageTooHigh_clampsToLastPage() {
        val items = (1..11).toList()
        val result = paginate(items, page = 99, pageSize = 10)
        assertEquals(listOf(11), result.items)
        assertEquals(2, result.currentPage)
        assertEquals(2, result.totalPages)
    }

    @Test
    fun paginate_negativePage_clampsToFirstPage() {
        val items = (1..11).toList()
        val result = paginate(items, page = -5, pageSize = 10)
        assertEquals((1..10).toList(), result.items)
        assertEquals(1, result.currentPage)
        assertEquals(2, result.totalPages)
    }
}
