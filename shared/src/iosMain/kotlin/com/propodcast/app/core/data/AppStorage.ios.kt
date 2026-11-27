package com.propodcast.app.core.data

import com.propodcast.app.core.database.buildDatabase
import com.propodcast.app.core.database.databaseBuilder

/** Process-wide [AppStorage], created on first use. */
val appStorage: AppStorage by lazy {
    AppStorage(
        database = buildDatabase(databaseBuilder()),
        userPreferences = createPreferencesDataStore(dataStorePath(DataStoreFiles.USER_PREFERENCES)),
        referenceCache = createPreferencesDataStore(dataStorePath(DataStoreFiles.REFERENCE_CACHE)),
    )
}
