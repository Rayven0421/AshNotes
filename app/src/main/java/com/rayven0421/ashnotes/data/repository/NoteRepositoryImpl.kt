package com.rayven0421.ashnotes.data.repository

import com.rayven0421.ashnotes.data.local.dao.NoteDao
import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import com.rayven0421.ashnotes.data.mapper.toDomain
import com.rayven0421.ashnotes.domain.model.Note
import com.rayven0421.ashnotes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun observeNotes(workspaceId: String): Flow<List<Note>> {
        return noteDao.observeNotes(workspaceId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNoteById(id: String): Note? {
        return noteDao.getNoteById(id)?.toDomain()
    }

    override suspend fun createNote(workspaceId: String, title: String): String {
        val note = NoteEntity(workspaceId = workspaceId, title = title)
        noteDao.insertNote(note)
        return note.id
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(
            NoteEntity(
                id = note.id,
                workspaceId = note.workspaceId,
                title = note.title,
                isPinned = note.isPinned,
                isArchived = note.isArchived,
                createdAt = note.createdAt,
                updatedAt = System.currentTimeMillis(),
                deletedAt = note.deletedAt
            )
        )
    }

    override suspend fun deleteNote(id: String) {
        noteDao.softDeleteNote(id)
    }
}
