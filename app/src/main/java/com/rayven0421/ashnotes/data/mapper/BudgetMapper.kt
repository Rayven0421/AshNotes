package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.BudgetEntity
import com.rayven0421.ashnotes.domain.model.Budget

fun BudgetEntity.toDomain() = Budget(
    id = id,
    workspaceId = workspaceId,
    name = name,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    startDate = startDate,
    endDate = endDate,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    workspaceId = workspaceId,
    name = name,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    startDate = startDate,
    endDate = endDate,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)
