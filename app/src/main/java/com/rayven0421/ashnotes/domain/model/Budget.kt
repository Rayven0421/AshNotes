package com.rayven0421.ashnotes.domain.model

import java.util.UUID

data class Budget(
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
