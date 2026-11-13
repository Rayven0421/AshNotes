package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.CloudBackupResult

interface CloudBackupRepository {
    suspend fun backup(): CloudBackupResult
    suspend fun restore(): CloudBackupResult
}
