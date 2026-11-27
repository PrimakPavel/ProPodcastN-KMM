package com.propodcast.app.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.propodcast.app.core.database.ProPodcastDatabase

/**
 * The app's database and DataStore files, one instance per process (DataStore fails when a file
 * is opened twice). Made by `appStorage()` in androidMain / iosMain.
 * Temporary until the DI stage, which will provide these as singletons.
 */
class AppStorage(
    val database: ProPodcastDatabase,
    val userPreferences: DataStore<Preferences>,
    val referenceCache: DataStore<Preferences>,
)
