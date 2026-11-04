package com.rayven0421.ashnotes.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "budgets",
    indices = [
        Index("workspaceId"),
        Index("deletedAt")
    ]
)
data class BudgetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val workspaceId: String,
    val name: String,
    val amountMinor: Long,
    val currencyCode: String = "PHP",
    val startDate: Long,
    val endDate: Long,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
