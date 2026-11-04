package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(workspaceId: String): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun createNote(workspaceId: String, title: String): String
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: String)
}
