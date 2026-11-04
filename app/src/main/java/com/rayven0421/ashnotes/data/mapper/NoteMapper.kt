package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import com.rayven0421.ashnotes.domain.model.Note

fun NoteEntity.toDomain() = Note(
    id = id,
    workspaceId = workspaceId,
    title = title,
    contentJson = contentJson,
    documentVersion = documentVersion,
    isPinned = isPinned,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)

fun Note.toEntity() = NoteEntity(
    id = id,
    workspaceId = workspaceId,
    title = title,
    contentJson = contentJson,
    documentVersion = documentVersion,
    isPinned = isPinned,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)
