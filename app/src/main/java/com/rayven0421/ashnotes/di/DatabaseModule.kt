package com.rayven0421.ashnotes.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rayven0421.ashnotes.data.local.AshNotesDatabase
import com.rayven0421.ashnotes.data.local.dao.BudgetDao
import com.rayven0421.ashnotes.data.local.dao.CategoryDao
import com.rayven0421.ashnotes.data.local.dao.ExpenseDao
import com.rayven0421.ashnotes.data.local.dao.NoteDao
import com.rayven0421.ashnotes.data.local.dao.SyncMetadataDao
import com.rayven0421.ashnotes.data.local.dao.SyncQueueDao
import com.rayven0421.ashnotes.data.local.dao.WorkspaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAshNotesDatabase(
        @ApplicationContext context: Context
    ): AshNotesDatabase {
        return Room.databaseBuilder(
            context,
            AshNotesDatabase::class.java,
            AshNotesDatabase.DATABASE_NAME
        )
            .addMigrations(AshNotesDatabase.MIGRATION_1_2)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val now = System.currentTimeMillis()
                    db.execSQL(
                        "INSERT INTO workspaces (id, name, createdAt, updatedAt) VALUES (?, ?, ?, ?)",
                        arrayOf<Any>("default_workspace", "Personal", now, now)
                    )
                    AshNotesDatabase.seedDefaultCategories(db)
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkspaceDao(database: AshNotesDatabase): WorkspaceDao {
        return database.workspaceDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: AshNotesDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(database: AshNotesDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: AshNotesDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AshNotesDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(database: AshNotesDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }

    @Provides
    @Singleton
    fun provideSyncMetadataDao(database: AshNotesDatabase): SyncMetadataDao {
        return database.syncMetadataDao()
    }
}
