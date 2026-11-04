package com.rayven0421.ashnotes.data.repository

import com.rayven0421.ashnotes.data.local.dao.NoteDao
import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import com.rayven0421.ashnotes.data.mapper.toDomain
import com.rayven0421.ashnotes.data.mapper.toEntity
import com.rayven0421.ashnotes.domain.model.Note
import com.rayven0421.ashnotes.domain.model.NoteSortOption
import com.rayven0421.ashnotes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun observeNotes(workspaceId: String, searchQuery: String, sortOption: NoteSortOption): Flow<List<Note>> {
        val flow = when (sortOption) {
            NoteSortOption.UPDATED -> noteDao.observeNotesByUpdated(workspaceId, searchQuery)
            NoteSortOption.CREATED -> noteDao.observeNotesByCreated(workspaceId, searchQuery)
            NoteSortOption.TITLE_ASC -> noteDao.observeNotesByTitle(workspaceId, searchQuery)
        }
        return flow.map { entities ->
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
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(id: String) {
        noteDao.softDeleteNote(id)
    }

    override suspend fun restoreNote(id: String) {
        noteDao.restoreNote(id)
    }

    override suspend fun bulkPinNotes(ids: List<String>, isPinned: Boolean) {
        if (ids.isNotEmpty()) {
            noteDao.bulkPinNotes(ids, isPinned)
        }
    }

    override suspend fun bulkArchiveNotes(ids: List<String>, isArchived: Boolean) {
        if (ids.isNotEmpty()) {
            noteDao.bulkArchiveNotes(ids, isArchived)
        }
    }

    override suspend fun bulkDeleteNotes(ids: List<String>) {
        if (ids.isNotEmpty()) {
            noteDao.bulkSoftDeleteNotes(ids)
        }
    }

    override suspend fun bulkRestoreNotes(ids: List<String>) {
        if (ids.isNotEmpty()) {
            noteDao.bulkRestoreNotes(ids)
        }
    }
}
