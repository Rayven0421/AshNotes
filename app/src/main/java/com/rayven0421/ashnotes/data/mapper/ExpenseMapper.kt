package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.ExpenseEntity
import com.rayven0421.ashnotes.domain.model.Expense

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    budgetId = budgetId,
    categoryId = categoryId,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    description = description,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    budgetId = budgetId,
    categoryId = categoryId,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    description = description,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)
