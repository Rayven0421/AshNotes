package com.rayven0421.ashnotes.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AshNotesDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2_createsNewTablesAndSeedsDefaultCategories() {
        // Create database v1
        val db = helper.createDatabase(TEST_DB, 1)

        val now = System.currentTimeMillis()
        db.execSQL("INSERT INTO workspaces (id, name, createdAt, updatedAt) VALUES ('w1', 'Work', $now, $now)")
        db.execSQL("INSERT INTO notes (id, workspaceId, title, isPinned, isArchived, createdAt, updatedAt) VALUES ('n1', 'w1', 'V1 Note', 0, 0, $now, $now)")

        db.close()

        // Run migration v1 to v2 and validate schema
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB,
            2,
            true,
            AshNotesDatabase.MIGRATION_1_2
        )

        // Verify existing note and workspace survived
        val cursorNote = migratedDb.query("SELECT * FROM notes WHERE id = 'n1'")
        cursorNote.moveToFirst()
        assertEquals("V1 Note", cursorNote.getString(cursorNote.getColumnIndexOrThrow("title")))
        cursorNote.close()

        // Verify new v2 default categories were seeded
        val cursorCategories = migratedDb.query("SELECT COUNT(*) FROM categories")
        cursorCategories.moveToFirst()
        assertTrue(cursorCategories.getInt(0) > 0)
        cursorCategories.close()
    }
}
