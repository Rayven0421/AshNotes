package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.CategoryEntity
import com.rayven0421.ashnotes.domain.model.Category

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)
