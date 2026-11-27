package com.propodcast.app.core.data

import android.content.Context
import com.propodcast.app.core.database.buildDatabase
import com.propodcast.app.core.database.databaseBuilder

private var instance: AppStorage? = null

/** Process-wide [AppStorage]; survives activity recreation. */
@Synchronized
fun appStorage(context: Context): AppStorage = instance ?: AppStorage(
    database = buildDatabase(databaseBuilder(context)),
    userPreferences = createPreferencesDataStore(dataStorePath(context, DataStoreFiles.USER_PREFERENCES)),
    referenceCache = createPreferencesDataStore(dataStorePath(context, DataStoreFiles.REFERENCE_CACHE)),
).also { instance = it }
