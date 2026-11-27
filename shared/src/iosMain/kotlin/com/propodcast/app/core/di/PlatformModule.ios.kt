package com.propodcast.app.core.di

import com.propodcast.app.core.data.DataStoreFiles
import com.propodcast.app.core.data.createPreferencesDataStore
import com.propodcast.app.core.data.dataStorePath
import com.propodcast.app.core.database.buildDatabase
import com.propodcast.app.core.database.databaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { buildDatabase(databaseBuilder()) }
    single(DataStoreQualifiers.UserPreferences) {
        createPreferencesDataStore(dataStorePath(DataStoreFiles.USER_PREFERENCES))
    }
    single(DataStoreQualifiers.ReferenceCache) {
        createPreferencesDataStore(dataStorePath(DataStoreFiles.REFERENCE_CACHE))
    }
}
