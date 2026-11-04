package com.rayven0421.ashnotes.domain.model

import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val budgetId: String,
    val categoryId: String,
    val amountMinor: Long,
    val currencyCode: String = "PHP",
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
