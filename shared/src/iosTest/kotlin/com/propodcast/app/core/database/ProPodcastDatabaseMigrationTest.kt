package com.propodcast.app.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.propodcast.app.core.testing.TestDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.getenv
import kotlinx.cinterop.toKString

/**
 * Version 1 (favorites only) → 2 (downloads) keeps the favorites. Runs on iOS with Room's multiplatform helper and
 * the bundled SQLite the app uses; the exported schemas come from shared/schemas (path set by Gradle).
 */
@OptIn(ExperimentalForeignApi::class)
class ProPodcastDatabaseMigrationTest {

    private val tmp = TestDirectory()

    private val helper = MigrationTestHelper(
        // The helper looks for <dir>/<database class>/<version>.json.
        schemaDirectoryPath = requireNotNull(getenv("ROOM_SCHEMAS_DIR")?.toKString()) { "ROOM_SCHEMAS_DIR not set" },
        fileName = (tmp.root / "migration-test.db").toString(),
        driver = BundledSQLiteDriver(),
        databaseClass = ProPodcastDatabase::class,
    )

    @AfterTest
    fun tearDown() {
        helper.finished()
        tmp.delete()
    }

    @Test
    fun migrate1To2_keepsFavorites_andAddsDownloads() {
        helper.createDatabase(1).execSQL(
            "INSERT INTO favorite_podcasts (id, title, publisher, image_url, thumbnail_url, description, " +
                "total_episodes, listennotes_url, latest_pub_date_ms, added_at) " +
                "VALUES ('p1', 'Podcast', 'Publisher', NULL, NULL, '', 3, NULL, NULL, 1)",
        )

        val db = helper.runMigrationsAndValidate(2, emptyList())
        db.prepare("SELECT title FROM favorite_podcasts").use { statement ->
            statement.step()
            assertEquals("Podcast", statement.getText(0))
        }
        db.prepare("SELECT COUNT(*) FROM downloaded_episodes").use { statement ->
            statement.step()
            assertEquals(0, statement.getInt(0))
        }
    }
}
