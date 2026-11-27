package com.propodcast.app.core.testing

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.propodcast.app.core.database.ProPodcastDatabase
import kotlinx.coroutines.Dispatchers
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// @RunWith is inherited, so every common subclass runs under Robolectric.
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
actual abstract class DatabaseTest actual constructor()

// The bundled SQLite driver ships Android binaries only, so host tests use the framework SQLite
// that Robolectric provides. The app itself uses the bundled driver on both platforms.
actual fun inMemoryTestDatabase(): ProPodcastDatabase =
    Room.inMemoryDatabaseBuilder<ProPodcastDatabase>(ApplicationProvider.getApplicationContext())
        .setQueryCoroutineContext(Dispatchers.IO)
        .allowMainThreadQueries()
        .build()
