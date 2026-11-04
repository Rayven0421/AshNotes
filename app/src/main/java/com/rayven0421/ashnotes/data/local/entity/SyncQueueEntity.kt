package com.rayven0421.ashnotes.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sync_queue",
    indices = [
        Index("entityId")
    ]
)
data class SyncQueueEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val operation: String,
    val createdAt: Long = System.currentTimeMillis()
)
