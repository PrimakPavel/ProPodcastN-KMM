package com.propodcast.app.core.di

import com.propodcast.app.core.connectivity.AndroidConnectivityMonitor
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.analytics.FirebaseAnalyticsTracker
import com.propodcast.app.core.analytics.NoOpAnalyticsTracker
import com.propodcast.app.core.crash.isFirebaseConfigured
import com.propodcast.app.core.connectivity.ConnectivityMonitor
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.download.DownloadNotifications
import com.propodcast.app.core.download.EpisodeDownloadWorker
import com.propodcast.app.core.download.EpisodeFileStore
import com.propodcast.app.core.download.WorkManagerDownloadsRepository
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import com.propodcast.app.core.player.MediaPlayerController
import com.propodcast.app.core.player.PlayerController
import com.propodcast.app.core.data.DataStoreFiles
import com.propodcast.app.core.data.createPreferencesDataStore
import com.propodcast.app.core.data.dataStorePath
import com.propodcast.app.core.database.buildDatabase
import com.propodcast.app.core.database.databaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Firebase only when the build had google-services.json (FirebaseApp was initialised).
    single<AnalyticsTracker> {
        if (isFirebaseConfigured(androidContext())) {
            FirebaseAnalyticsTracker(FirebaseAnalytics.getInstance(androidContext()))
        } else {
            NoOpAnalyticsTracker
        }
    }
    single<ConnectivityMonitor> { AndroidConnectivityMonitor(androidContext()) }
    single { WorkManager.getInstance(androidContext()) }
    single { EpisodeFileStore(androidContext()) }
    single { DownloadNotifications(androidContext()) }
    // Audio hosts can be slow; follows redirects (Listen Notes audio links redirect to the host).
    single(EpisodeDownloadWorker.DOWNLOAD_CLIENT) {
        OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()
    }
    single<DownloadsRepository> {
        WorkManagerDownloadsRepository(
            dao = get(),
            workManager = get(),
            files = get(),
            analytics = get(),
            preferences = get(),
        )
    }
    single<PlayerController> {
        MediaPlayerController(androidContext(), preferences = get(), downloads = get(), analytics = get())
    }
    single { buildDatabase(databaseBuilder(androidContext())) }
    single(DataStoreQualifiers.UserPreferences) {
        createPreferencesDataStore(dataStorePath(androidContext(), DataStoreFiles.USER_PREFERENCES))
    }
    single(DataStoreQualifiers.ReferenceCache) {
        createPreferencesDataStore(dataStorePath(androidContext(), DataStoreFiles.REFERENCE_CACHE))
    }
}
