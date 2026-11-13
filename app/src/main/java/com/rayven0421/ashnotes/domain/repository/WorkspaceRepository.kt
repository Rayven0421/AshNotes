package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.CreateWorkspaceResult
import com.rayven0421.ashnotes.domain.model.Workspace
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {
    val workspaces: Flow<List<Workspace>>
    val activeWorkspaceId: Flow<String>

    fun observeWorkspaces(): Flow<List<Workspace>> = workspaces
    fun observeActiveWorkspaceId(): Flow<String> = activeWorkspaceId

    suspend fun getWorkspaceById(id: String): Workspace?
    suspend fun createWorkspace(name: String): CreateWorkspaceResult
    suspend fun setActive(id: String)
    suspend fun updateWorkspace(workspace: Workspace)
    suspend fun deleteWorkspace(id: String)
    suspend fun clearAllLocalData()
}
