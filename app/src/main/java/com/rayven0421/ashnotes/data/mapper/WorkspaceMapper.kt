package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.WorkspaceEntity
import com.rayven0421.ashnotes.domain.model.Workspace

fun WorkspaceEntity.toDomain() = Workspace(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)

fun Workspace.toEntity() = WorkspaceEntity(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)
