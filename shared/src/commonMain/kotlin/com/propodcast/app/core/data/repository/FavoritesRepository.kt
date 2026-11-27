package com.propodcast.app.core.data.repository

import com.propodcast.app.core.analytics.AnalyticsEvents
import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.database.dao.FavoriteEpisodeDao
import com.propodcast.app.core.database.dao.FavoritePodcastDao
import com.propodcast.app.core.database.entity.toFavoriteEntity
import com.propodcast.app.core.database.entity.toModel
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.RemovedFavorite
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Favorite podcasts and episodes stored locally, newest first. */
interface FavoritesRepository {
    val favoritePodcasts: Flow<List<Podcast>>
    val favoriteEpisodes: Flow<List<Episode>>

    /** Ids for marking list items as favorite without loading full rows. */
    val favoritePodcastIds: Flow<Set<String>>
    val favoriteEpisodeIds: Flow<Set<String>>

    suspend fun setPodcastFavorite(podcast: Podcast, favorite: Boolean)
    suspend fun setEpisodeFavorite(episode: Episode, favorite: Boolean)

    /** Removes a favorite and returns it for [restore]; null when it was not a favorite. */
    suspend fun removePodcast(id: String): RemovedFavorite.PodcastItem?
    suspend fun removeEpisode(id: String): RemovedFavorite.EpisodeItem?

    /** Puts a removed favorite back with its original date, so it returns to the same position. */
    suspend fun restore(removed: RemovedFavorite)
}

class DefaultFavoritesRepository(
    private val podcastDao: FavoritePodcastDao,
    private val episodeDao: FavoriteEpisodeDao,
    private val analytics: AnalyticsTracker,
    private val clock: Clock = Clock.System,
) : FavoritesRepository {

    override val favoritePodcasts: Flow<List<Podcast>> =
        podcastDao.observeAll().map { rows -> rows.map { it.toModel() } }

    override val favoriteEpisodes: Flow<List<Episode>> =
        episodeDao.observeAll().map { rows -> rows.map { it.toModel() } }

    override val favoritePodcastIds: Flow<Set<String>> = podcastDao.observeIds().map { it.toSet() }

    override val favoriteEpisodeIds: Flow<Set<String>> = episodeDao.observeIds().map { it.toSet() }

    override suspend fun setPodcastFavorite(podcast: Podcast, favorite: Boolean) {
        if (favorite) {
            podcastDao.upsert(podcast.toFavoriteEntity(addedAt = clock.now().toEpochMilliseconds()))
            analytics.logEvent(AnalyticsEvents.ADD_FAVORITE, mapOf(AnalyticsEvents.PARAM_TYPE to AnalyticsEvents.TYPE_PODCAST))
        } else {
            podcastDao.delete(podcast.id)
        }
    }

    override suspend fun setEpisodeFavorite(episode: Episode, favorite: Boolean) {
        if (favorite) {
            episodeDao.upsert(episode.toFavoriteEntity(addedAt = clock.now().toEpochMilliseconds()))
            analytics.logEvent(AnalyticsEvents.ADD_FAVORITE, mapOf(AnalyticsEvents.PARAM_TYPE to AnalyticsEvents.TYPE_EPISODE))
        } else {
            episodeDao.delete(episode.id)
        }
    }

    override suspend fun removePodcast(id: String): RemovedFavorite.PodcastItem? =
        podcastDao.remove(id)?.let { RemovedFavorite.PodcastItem(it.toModel(), it.addedAt) }

    override suspend fun removeEpisode(id: String): RemovedFavorite.EpisodeItem? =
        episodeDao.remove(id)?.let { RemovedFavorite.EpisodeItem(it.toModel(), it.addedAt) }

    override suspend fun restore(removed: RemovedFavorite) {
        when (removed) {
            is RemovedFavorite.PodcastItem -> podcastDao.upsert(removed.podcast.toFavoriteEntity(removed.addedAtMs))
            is RemovedFavorite.EpisodeItem -> episodeDao.upsert(removed.episode.toFavoriteEntity(removed.addedAtMs))
        }
    }
}
