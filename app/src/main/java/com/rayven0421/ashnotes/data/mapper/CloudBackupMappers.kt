package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.BudgetEntity
import com.rayven0421.ashnotes.data.local.entity.CategoryEntity
import com.rayven0421.ashnotes.data.local.entity.ExpenseEntity
import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import com.rayven0421.ashnotes.data.local.entity.WorkspaceEntity

object NoteSizeChecker {
    const val MAX_NOTE_BYTES: Long = 900 * 1024 // 900 KB threshold (Firestore doc limit is 1 MiB)

    fun isTooLarge(contentJson: String): Boolean {
        return contentJson.toByteArray(Charsets.UTF_8).size > MAX_NOTE_BYTES
    }
}

object RestoreMergeDecision {
    fun <T> filterNewItems(remoteItems: List<T>, existingIds: Set<String>, getId: (T) -> String): List<T> {
        return remoteItems.filter { getId(it) !in existingIds }
    }
}

fun NoteEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "workspaceId" to workspaceId,
    "title" to title,
    "contentJson" to contentJson,
    "documentVersion" to documentVersion,
    "isPinned" to isPinned,
    "isArchived" to isArchived,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "deletedAt" to deletedAt
)

fun noteEntityFromMap(map: Map<String, Any?>): NoteEntity = NoteEntity(
    id = map["id"] as? String ?: "",
    workspaceId = map["workspaceId"] as? String ?: "",
    title = map["title"] as? String ?: "",
    contentJson = map["contentJson"] as? String ?: "",
    documentVersion = (map["documentVersion"] as? Number)?.toInt() ?: 1,
    isPinned = map["isPinned"] as? Boolean ?: false,
    isArchived = map["isArchived"] as? Boolean ?: false,
    createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
    updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L,
    deletedAt = (map["deletedAt"] as? Number)?.toLong()
)

fun BudgetEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "workspaceId" to workspaceId,
    "name" to name,
    "amountMinor" to amountMinor,
    "currencyCode" to currencyCode,
    "startDate" to startDate,
    "endDate" to endDate,
    "isArchived" to isArchived,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "deletedAt" to deletedAt
)

fun budgetEntityFromMap(map: Map<String, Any?>): BudgetEntity = BudgetEntity(
    id = map["id"] as? String ?: "",
    workspaceId = map["workspaceId"] as? String ?: "",
    name = map["name"] as? String ?: "",
    amountMinor = (map["amountMinor"] as? Number)?.toLong() ?: 0L,
    currencyCode = map["currencyCode"] as? String ?: "PHP",
    startDate = (map["startDate"] as? Number)?.toLong() ?: 0L,
    endDate = (map["endDate"] as? Number)?.toLong() ?: 0L,
    isArchived = map["isArchived"] as? Boolean ?: false,
    createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
    updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L,
    deletedAt = (map["deletedAt"] as? Number)?.toLong()
)

fun ExpenseEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "budgetId" to budgetId,
    "categoryId" to categoryId,
    "amountMinor" to amountMinor,
    "currencyCode" to currencyCode,
    "description" to description,
    "date" to date,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "deletedAt" to deletedAt
)

fun expenseEntityFromMap(map: Map<String, Any?>): ExpenseEntity = ExpenseEntity(
    id = map["id"] as? String ?: "",
    budgetId = map["budgetId"] as? String ?: "",
    categoryId = map["categoryId"] as? String ?: "",
    amountMinor = (map["amountMinor"] as? Number)?.toLong() ?: 0L,
    currencyCode = map["currencyCode"] as? String ?: "PHP",
    description = map["description"] as? String ?: "",
    date = (map["date"] as? Number)?.toLong() ?: 0L,
    createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
    updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L,
    deletedAt = (map["deletedAt"] as? Number)?.toLong()
)

fun CategoryEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "icon" to icon,
    "color" to color,
    "isDefault" to isDefault,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "deletedAt" to deletedAt
)

fun categoryEntityFromMap(map: Map<String, Any?>): CategoryEntity = CategoryEntity(
    id = map["id"] as? String ?: "",
    name = map["name"] as? String ?: "",
    icon = map["icon"] as? String ?: "",
    color = map["color"] as? String ?: "",
    isDefault = map["isDefault"] as? Boolean ?: false,
    createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
    updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L,
    deletedAt = (map["deletedAt"] as? Number)?.toLong()
)

fun WorkspaceEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "deletedAt" to deletedAt
)

fun workspaceEntityFromMap(map: Map<String, Any?>): WorkspaceEntity = WorkspaceEntity(
    id = map["id"] as? String ?: "",
    name = map["name"] as? String ?: "",
    createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
    updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L,
    deletedAt = (map["deletedAt"] as? Number)?.toLong()
)
