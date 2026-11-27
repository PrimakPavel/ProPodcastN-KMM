package com.propodcast.app.core.di

import com.propodcast.app.core.data.cache.DataStoreReferenceDataCache
import com.propodcast.app.core.data.cache.ReferenceDataCache
import com.propodcast.app.core.data.repository.DataStoreUserPreferencesRepository
import com.propodcast.app.core.data.repository.DefaultFavoritesRepository
import com.propodcast.app.core.data.repository.DefaultPodcastRepository
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.database.ProPodcastDatabase
import kotlin.time.Clock
import org.koin.dsl.module

/** Repositories and DAOs. The database, DataStores and analytics come from the platform module. */
val dataModule = module {
    single<Clock> { Clock.System }

    single { get<ProPodcastDatabase>().favoritePodcastDao() }
    single { get<ProPodcastDatabase>().favoriteEpisodeDao() }
    single { get<ProPodcastDatabase>().downloadedEpisodeDao() }

    single<ReferenceDataCache> {
        DataStoreReferenceDataCache(
            dataStore = get(DataStoreQualifiers.ReferenceCache),
            json = get(),
            apiConfig = get(),
            clock = get(),
        )
    }
    single<UserPreferencesRepository> { DataStoreUserPreferencesRepository(get(DataStoreQualifiers.UserPreferences)) }
    single<PodcastRepository> { DefaultPodcastRepository(api = get(), cache = get(), analytics = get()) }
    single<FavoritesRepository> {
        DefaultFavoritesRepository(podcastDao = get(), episodeDao = get(), analytics = get(), clock = get())
    }
}
