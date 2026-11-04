package com.rayven0421.ashnotes.data.repository

import com.rayven0421.ashnotes.data.local.dao.WorkspaceDao
import com.rayven0421.ashnotes.data.local.entity.WorkspaceEntity
import com.rayven0421.ashnotes.data.mapper.toDomain
import com.rayven0421.ashnotes.domain.model.Workspace
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkspaceRepositoryImpl @Inject constructor(
    private val workspaceDao: WorkspaceDao
) : WorkspaceRepository {

    override fun observeWorkspaces(): Flow<List<Workspace>> {
        return workspaceDao.observeWorkspaces().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getWorkspaceById(id: String): Workspace? {
        return workspaceDao.getWorkspaceById(id)?.toDomain()
    }

    override suspend fun createWorkspace(name: String): String {
        val workspace = WorkspaceEntity(name = name)
        workspaceDao.insertWorkspace(workspace)
        return workspace.id
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
}
