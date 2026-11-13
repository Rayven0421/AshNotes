package com.rayven0421.ashnotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE workspaceId = :workspaceId AND deletedAt IS NULL AND isArchived = :isArchived AND (title LIKE '%' || :query || '%' OR :query = '') ORDER BY isPinned DESC, updatedAt DESC")
    fun observeNotesByUpdated(workspaceId: String, query: String = "", isArchived: Boolean = false): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE workspaceId = :workspaceId AND deletedAt IS NULL AND isArchived = :isArchived AND (title LIKE '%' || :query || '%' OR :query = '') ORDER BY isPinned DESC, createdAt DESC")
    fun observeNotesByCreated(workspaceId: String, query: String = "", isArchived: Boolean = false): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE workspaceId = :workspaceId AND deletedAt IS NULL AND isArchived = :isArchived AND (title LIKE '%' || :query || '%' OR :query = '') ORDER BY isPinned DESC, title ASC")
    fun observeNotesByTitle(workspaceId: String, query: String = "", isArchived: Boolean = false): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id AND deletedAt IS NULL")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesIncludingDeleted(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteNote(id: String, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET deletedAt = NULL WHERE id = :id")
    suspend fun restoreNote(id: String)

    @Transaction
    @Query("UPDATE notes SET isPinned = :isPinned WHERE id IN (:ids)")
    suspend fun bulkPinNotes(ids: List<String>, isPinned: Boolean)

    @Transaction
    @Query("UPDATE notes SET isArchived = :isArchived WHERE id IN (:ids)")
    suspend fun bulkArchiveNotes(ids: List<String>, isArchived: Boolean)

    @Transaction
    @Query("UPDATE notes SET deletedAt = :deletedAt WHERE id IN (:ids)")
    suspend fun bulkSoftDeleteNotes(ids: List<String>, deletedAt: Long = System.currentTimeMillis())

    @Transaction
    @Query("UPDATE notes SET deletedAt = NULL WHERE id IN (:ids)")
    suspend fun bulkRestoreNotes(ids: List<String>)
}
