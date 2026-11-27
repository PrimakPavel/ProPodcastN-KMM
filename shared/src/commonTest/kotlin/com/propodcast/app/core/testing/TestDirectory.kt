package com.propodcast.app.core.testing

import kotlin.random.Random
import okio.FileSystem
import okio.Path

/** A fresh folder under the system temp directory (replaces JUnit's TemporaryFolder in common tests). */
class TestDirectory {
    val root: Path = (FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "propodcast-test-${Random.nextLong().toULong()}")
        .also { FileSystem.SYSTEM.createDirectories(it) }

    fun delete() = FileSystem.SYSTEM.deleteRecursively(root)
}
