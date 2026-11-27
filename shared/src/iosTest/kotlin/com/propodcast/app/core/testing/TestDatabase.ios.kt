package com.propodcast.app.core.testing

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.propodcast.app.core.database.ProPodcastDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual abstract class DatabaseTest actual constructor()

actual fun inMemoryTestDatabase(): ProPodcastDatabase =
    Room.inMemoryDatabaseBuilder<ProPodcastDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

actual abstract class UiTestBase actual constructor()
