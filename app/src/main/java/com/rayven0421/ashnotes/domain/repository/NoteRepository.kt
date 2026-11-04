package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.Note
import com.rayven0421.ashnotes.domain.model.NoteSortOption
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(workspaceId: String, searchQuery: String = "", sortOption: NoteSortOption = NoteSortOption.UPDATED): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun createNote(workspaceId: String, title: String): String
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: String)
    suspend fun restoreNote(id: String)
    suspend fun bulkPinNotes(ids: List<String>, isPinned: Boolean)
    suspend fun bulkArchiveNotes(ids: List<String>, isArchived: Boolean)
    suspend fun bulkDeleteNotes(ids: List<String>)
    suspend fun bulkRestoreNotes(ids: List<String>)
}
