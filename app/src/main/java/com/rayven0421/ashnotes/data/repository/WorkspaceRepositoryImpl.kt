package com.rayven0421.ashnotes.data.repository

import com.rayven0421.ashnotes.data.local.AshNotesDatabase
import com.rayven0421.ashnotes.data.local.dao.WorkspaceDao
import com.rayven0421.ashnotes.data.local.entity.WorkspaceEntity
import com.rayven0421.ashnotes.data.mapper.toDomain
import com.rayven0421.ashnotes.domain.model.CreateWorkspaceResult
import com.rayven0421.ashnotes.domain.model.Workspace
import com.rayven0421.ashnotes.domain.model.resolveActiveWorkspaceId
import com.rayven0421.ashnotes.domain.model.validateWorkspaceName
import com.rayven0421.ashnotes.domain.repository.UserPreferencesRepository
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkspaceRepositoryImpl @Inject constructor(
    private val workspaceDao: WorkspaceDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val database: AshNotesDatabase
) : WorkspaceRepository {

    override val workspaces: Flow<List<Workspace>> = workspaceDao.observeWorkspaces().map { entities ->
        entities.map { it.toDomain() }
    }

    override val activeWorkspaceId: Flow<String> = combine(
        userPreferencesRepository.activeWorkspaceId,
        workspaces
    ) { storedId, list ->
        resolveActiveWorkspaceId(storedId, list)
    }

    override suspend fun getWorkspaceById(id: String): Workspace? {
        return workspaceDao.getWorkspaceById(id)?.toDomain()
    }

    override suspend fun createWorkspace(name: String): CreateWorkspaceResult {
        val currentWorkspaces = workspaces.first()
        val error = validateWorkspaceName(name, currentWorkspaces)
        if (error != null) {
            return CreateWorkspaceResult.Error(error)
        }
        val workspace = WorkspaceEntity(name = name.trim())
        workspaceDao.insertWorkspace(workspace)
        setActive(workspace.id)
        return CreateWorkspaceResult.Success(workspace.id)
    }

    override suspend fun setActive(id: String) {
        userPreferencesRepository.setActiveWorkspaceId(id)
    }

    override suspend fun updateWorkspace(workspace: Workspace) {
        workspaceDao.updateWorkspace(
            WorkspaceEntity(
                id = workspace.id,
                name = workspace.name,
                createdAt = workspace.createdAt,
                updatedAt = System.currentTimeMillis(),
                deletedAt = workspace.deletedAt
            )
        )
    }

    override suspend fun deleteWorkspace(id: String) {
        workspaceDao.softDeleteWorkspace(id)
    }

    override suspend fun clearAllLocalData() {
        database.clearAllLocalData()
        userPreferencesRepository.setActiveWorkspaceId("default_workspace")
    }
}
