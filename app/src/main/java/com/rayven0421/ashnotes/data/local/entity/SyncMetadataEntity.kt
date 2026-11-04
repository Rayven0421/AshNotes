package com.rayven0421.ashnotes.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sync_metadata",
    indices = [
        Index("entityId")
    ]
)
data class SyncMetadataEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val serverVersion: Long = 0L,
    val lastSyncedAt: Long? = null,
    val isDirty: Boolean = false
)
