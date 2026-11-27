package com.propodcast.app.core.di

import com.propodcast.app.core.data.DataStoreFiles
import com.propodcast.app.core.data.createPreferencesDataStore
import com.propodcast.app.core.data.dataStorePath
import com.propodcast.app.core.database.buildDatabase
import com.propodcast.app.core.database.databaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { buildDatabase(databaseBuilder(androidContext())) }
    single(DataStoreQualifiers.UserPreferences) {
        createPreferencesDataStore(dataStorePath(androidContext(), DataStoreFiles.USER_PREFERENCES))
    }
    single(DataStoreQualifiers.ReferenceCache) {
        createPreferencesDataStore(dataStorePath(androidContext(), DataStoreFiles.REFERENCE_CACHE))
    }
}
