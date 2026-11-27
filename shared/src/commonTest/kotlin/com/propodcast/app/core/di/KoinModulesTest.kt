package com.propodcast.app.core.di

import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.analytics.NoOpAnalyticsTracker
import com.propodcast.app.core.data.cache.ReferenceDataCache
import com.propodcast.app.core.data.createPreferencesDataStore
import com.propodcast.app.core.data.repository.ApiQuotaRepository
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.database.dao.DownloadedEpisodeDao
import com.propodcast.app.core.database.dao.FavoriteEpisodeDao
import com.propodcast.app.core.database.dao.FavoritePodcastDao
import com.propodcast.app.core.network.ApiConfig
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.testing.DatabaseTest
import com.propodcast.app.core.testing.TestDirectory
import com.propodcast.app.core.testing.inMemoryTestDatabase
import io.ktor.client.HttpClient
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertSame
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** The whole graph resolves: catches a missing or misnamed definition before the app crashes on it. */
class KoinModulesTest : DatabaseTest() {

    private val tmp = TestDirectory()

    @AfterTest
    fun deleteTempFiles() = tmp.delete()

    /** In-memory database and temp DataStores instead of the platform files. */
    private val testPlatformModule = module {
        single<AnalyticsTracker> { NoOpAnalyticsTracker }
        single { inMemoryTestDatabase() }
        single(DataStoreQualifiers.UserPreferences) { createPreferencesDataStore((tmp.root / "prefs.preferences_pb").toString()) }
        single(DataStoreQualifiers.ReferenceCache) { createPreferencesDataStore((tmp.root / "cache.preferences_pb").toString()) }
    }

    @Test
    fun allDefinitions_resolve() {
        val koin = koinApplication { modules(appModules(platform = testPlatformModule)) }.koin
        try {
            koin.get<ApiConfig>()
            koin.get<HttpClient>()
            koin.get<ListenNotesApi>()
            koin.get<AnalyticsTracker>()
            koin.get<FavoritePodcastDao>()
            koin.get<FavoriteEpisodeDao>()
            koin.get<DownloadedEpisodeDao>()
            koin.get<ReferenceDataCache>()
            koin.get<UserPreferencesRepository>()
            koin.get<PodcastRepository>()
            koin.get<FavoritesRepository>()
            // One quota repository: the HTTP client reports into the same instance the UI reads.
            assertSame(koin.get<ApiQuotaRepository>(), koin.get<com.propodcast.app.core.data.repository.InMemoryApiQuotaRepository>())
        } finally {
            koin.close()
        }
    }
}
