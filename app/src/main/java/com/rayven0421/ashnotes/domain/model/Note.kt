package com.rayven0421.ashnotes.domain.model

data class Note(
    val id: String,
    val workspaceId: String,
    val title: String,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?
)
