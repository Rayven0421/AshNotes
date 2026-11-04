package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import com.rayven0421.ashnotes.data.local.entity.WorkspaceEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    @Test
    fun workspaceEntityToDomain_mapsCorrectly() {
        val entity = WorkspaceEntity(
            id = "w1",
            name = "Work",
            createdAt = 1000L,
            updatedAt = 2000L,
            deletedAt = null
        )
        val domain = entity.toDomain()
        assertEquals("w1", domain.id)
        assertEquals("Work", domain.name)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
        assertEquals(null, domain.deletedAt)

        val backToEntity = domain.toEntity()
        assertEquals(entity, backToEntity)
    }

    @Test
    fun noteEntityToDomain_mapsCorrectly() {
        val entity = NoteEntity(
            id = "n1",
            workspaceId = "w1",
            title = "Meeting Notes",
            isPinned = true,
            isArchived = false,
            createdAt = 1000L,
            updatedAt = 2000L,
            deletedAt = null
        )
        val domain = entity.toDomain()
        assertEquals("n1", domain.id)
        assertEquals("w1", domain.workspaceId)
        assertEquals("Meeting Notes", domain.title)
        assertEquals(true, domain.isPinned)
        assertEquals(false, domain.isArchived)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
        assertEquals(null, domain.deletedAt)

        val backToEntity = domain.toEntity()
        assertEquals(entity, backToEntity)
    }
}
