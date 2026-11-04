package com.rayven0421.ashnotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rayven0421.ashnotes.data.local.dao.BudgetDao
import com.rayven0421.ashnotes.data.local.dao.CategoryDao
import com.rayven0421.ashnotes.data.local.dao.ExpenseDao
import com.rayven0421.ashnotes.data.local.dao.NoteDao
import com.rayven0421.ashnotes.data.local.dao.SyncMetadataDao
import com.rayven0421.ashnotes.data.local.dao.SyncQueueDao
import com.rayven0421.ashnotes.data.local.dao.WorkspaceDao
import com.rayven0421.ashnotes.data.local.entity.BudgetEntity
import com.rayven0421.ashnotes.data.local.entity.CategoryEntity
import com.rayven0421.ashnotes.data.local.entity.ExpenseEntity
import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import com.rayven0421.ashnotes.data.local.entity.SyncMetadataEntity
import com.rayven0421.ashnotes.data.local.entity.SyncQueueEntity
import com.rayven0421.ashnotes.data.local.entity.WorkspaceEntity
import androidx.room.withTransaction
import java.util.UUID

@Database(
    entities = [
        WorkspaceEntity::class,
        NoteEntity::class,
        BudgetEntity::class,
        ExpenseEntity::class,
        CategoryEntity::class,
        SyncQueueEntity::class,
        SyncMetadataEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AshNotesDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun noteDao(): NoteDao
    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    suspend fun clearAllLocalData() {
        withTransaction {
            clearAllTables()
            val now = System.currentTimeMillis()
            workspaceDao().insertWorkspace(
                WorkspaceEntity(
                    id = "default_workspace",
                    name = "Personal",
                    createdAt = now,
                    updatedAt = now
                )
            )
            val defaultCategories = listOf(
                Triple("Food", "restaurant", "#875FFF"),
                Triple("Transportation", "directions_car", "#86B89C"),
                Triple("Shopping", "shopping_bag", "#E9795A"),
                Triple("Entertainment", "movie", "#5FA8FF"),
                Triple("Bills", "receipt", "#FFB85F"),
                Triple("Education", "school", "#A39FB4"),
                Triple("Health", "favorite", "#FF5F87"),
                Triple("Other", "category", "#B886B8")
            )
            for ((name, icon, color) in defaultCategories) {
                val id = UUID.nameUUIDFromBytes(name.toByteArray()).toString()
                categoryDao().insertCategory(
                    CategoryEntity(
                        id = id,
                        name = name,
                        icon = icon,
                        color = color,
                        isDefault = true,
                        createdAt = now,
                        updatedAt = now,
                        deletedAt = null
                    )
                )
            }
        }
    }

    companion object {
        const val DATABASE_NAME = "ashnotes.db"

        fun seedDefaultCategories(db: SupportSQLiteDatabase) {
            val defaultCategories = listOf(
                Triple("Food", "restaurant", "#875FFF"),
                Triple("Transportation", "directions_car", "#86B89C"),
                Triple("Shopping", "shopping_bag", "#E9795A"),
                Triple("Entertainment", "movie", "#5FA8FF"),
                Triple("Bills", "receipt", "#FFB85F"),
                Triple("Education", "school", "#A39FB4"),
                Triple("Health", "favorite", "#FF5F87"),
                Triple("Other", "category", "#B886B8")
            )
            val now = System.currentTimeMillis()
            for ((name, icon, color) in defaultCategories) {
                val id = UUID.nameUUIDFromBytes(name.toByteArray()).toString()
                db.execSQL("""
                    INSERT OR IGNORE INTO categories (id, name, icon, color, isDefault, createdAt, updatedAt, deletedAt)
                    VALUES ('$id', '$name', '$icon', '$color', 1, $now, $now, NULL)
                """)
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `budgets` (
                        `id` TEXT NOT NULL,
                        `workspaceId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `amountMinor` INTEGER NOT NULL,
                        `currencyCode` TEXT NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER NOT NULL,
                        `isArchived` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `deletedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_workspaceId` ON `budgets` (`workspaceId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_deletedAt` ON `budgets` (`deletedAt`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `expenses` (
                        `id` TEXT NOT NULL,
                        `budgetId` TEXT NOT NULL,
                        `categoryId` TEXT NOT NULL,
                        `amountMinor` INTEGER NOT NULL,
                        `currencyCode` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `date` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `deletedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_budgetId` ON `expenses` (`budgetId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_deletedAt` ON `expenses` (`deletedAt`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `icon` TEXT NOT NULL,
                        `color` TEXT NOT NULL,
                        `isDefault` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `deletedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_deletedAt` ON `categories` (`deletedAt`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sync_queue` (
                        `id` TEXT NOT NULL,
                        `entityType` TEXT NOT NULL,
                        `entityId` TEXT NOT NULL,
                        `operation` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_queue_entityId` ON `sync_queue` (`entityId`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sync_metadata` (
                        `id` TEXT NOT NULL,
                        `entityType` TEXT NOT NULL,
                        `entityId` TEXT NOT NULL,
                        `serverVersion` INTEGER NOT NULL,
                        `lastSyncedAt` INTEGER,
                        `isDirty` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_metadata_entityId` ON `sync_metadata` (`entityId`)")

                seedDefaultCategories(db)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `contentJson` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `documentVersion` INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
