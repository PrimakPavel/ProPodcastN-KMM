package com.propodcast.app.core.di

import com.propodcast.app.core.connectivity.ConnectivityMonitor
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.download.IosDownloadsRepository
import com.propodcast.app.core.connectivity.IosConnectivityMonitor
import com.propodcast.app.core.player.IosPlayerController
import com.propodcast.app.core.player.PlayerController
import com.propodcast.app.core.data.DataStoreFiles
import com.propodcast.app.core.data.createPreferencesDataStore
import com.propodcast.app.core.data.dataStorePath
import com.propodcast.app.core.database.buildDatabase
import com.propodcast.app.core.database.databaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ConnectivityMonitor> { IosConnectivityMonitor() }
    // Created at start: the background URLSession must exist when iOS relaunches the app to deliver downloads.
    single<DownloadsRepository>(createdAtStart = true) {
        IosDownloadsRepository(dao = get(), analytics = get(), preferences = get())
    }
    single<PlayerController> { IosPlayerController(preferences = get(), downloads = get(), analytics = get()) }
    single { buildDatabase(databaseBuilder()) }
    single(DataStoreQualifiers.UserPreferences) {
        createPreferencesDataStore(dataStorePath(DataStoreFiles.USER_PREFERENCES))
    }
    single(DataStoreQualifiers.ReferenceCache) {
        createPreferencesDataStore(dataStorePath(DataStoreFiles.REFERENCE_CACHE))
    }
}
