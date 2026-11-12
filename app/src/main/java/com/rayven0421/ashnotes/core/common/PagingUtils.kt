package com.rayven0421.ashnotes.core.common

const val BUDGET_PAGE_SIZE = 10

data class PaginationResult<T>(
    val items: List<T>,
    val currentPage: Int,
    val totalPages: Int
)

fun <T> paginate(items: List<T>, page: Int, pageSize: Int = BUDGET_PAGE_SIZE): PaginationResult<T> {
    val validPageSize = if (pageSize <= 0) BUDGET_PAGE_SIZE else pageSize
    val totalPages = if (items.isEmpty()) 1 else (items.size + validPageSize - 1) / validPageSize
    val clampedPage = page.coerceIn(1, totalPages)
    if (items.isEmpty()) {
        return PaginationResult(
            items = emptyList(),
            currentPage = 1,
            totalPages = 1
        )
    }
    val startIndex = (clampedPage - 1) * validPageSize
    val endIndex = minOf(startIndex + validPageSize, items.size)
    val pageItems = items.subList(startIndex, endIndex)
    return PaginationResult(
        items = pageItems,
        currentPage = clampedPage,
        totalPages = totalPages
    )
}
