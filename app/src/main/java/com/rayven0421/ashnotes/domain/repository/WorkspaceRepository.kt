package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.Workspace
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {
    fun observeWorkspaces(): Flow<List<Workspace>>
    fun observeActiveWorkspaceId(): Flow<String>
    suspend fun getWorkspaceById(id: String): Workspace?
    suspend fun createWorkspace(name: String): String
    suspend fun updateWorkspace(workspace: Workspace)
    suspend fun deleteWorkspace(id: String)
    suspend fun clearAllLocalData()
}
