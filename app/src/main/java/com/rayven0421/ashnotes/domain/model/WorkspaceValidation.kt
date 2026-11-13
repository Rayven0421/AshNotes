package com.rayven0421.ashnotes.domain.model

sealed interface WorkspaceNameValidationError {
    object Empty : WorkspaceNameValidationError
    object TooLong : WorkspaceNameValidationError
    object Duplicate : WorkspaceNameValidationError
}

sealed interface CreateWorkspaceResult {
    data class Success(val workspaceId: String) : CreateWorkspaceResult
    data class Error(val reason: WorkspaceNameValidationError) : CreateWorkspaceResult
}

fun validateWorkspaceName(name: String, existingWorkspaces: List<Workspace>): WorkspaceNameValidationError? {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return WorkspaceNameValidationError.Empty
    if (trimmed.length > 40) return WorkspaceNameValidationError.TooLong
    if (existingWorkspaces.any { it.deletedAt == null && it.name.trim().equals(trimmed, ignoreCase = true) }) {
        return WorkspaceNameValidationError.Duplicate
    }
    return null
}

fun resolveActiveWorkspaceId(storedId: String?, existingWorkspaces: List<Workspace>): String {
    if (storedId != null && existingWorkspaces.any { it.id == storedId && it.deletedAt == null }) {
        return storedId
    }
    return "default_workspace"
}
