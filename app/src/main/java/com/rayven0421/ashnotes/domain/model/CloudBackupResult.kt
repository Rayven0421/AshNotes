package com.rayven0421.ashnotes.domain.model

enum class CloudBackupError {
    NOT_SIGNED_IN,
    NETWORK,
    PERMISSION_DENIED,
    UNKNOWN
}

sealed interface CloudBackupResult {
    data class Success(
        val uploaded: Int = 0,
        val added: Int = 0,
        val skippedLarge: Int = 0
    ) : CloudBackupResult

    data class Error(val error: CloudBackupError) : CloudBackupResult
}
