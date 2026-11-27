package com.propodcast.app.core.testing

import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.RemovedFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory favorites, newest first like the Room queries; [addedAt] ticks on every add. */
class FakeFavoritesRepository : FavoritesRepository {
    private var clock = 0L
    private val addedAt = mutableMapOf<String, Long>()

    private val podcasts = MutableStateFlow<List<Podcast>>(emptyList())
    private val episodes = MutableStateFlow<List<Episode>>(emptyList())

    override val favoritePodcasts: Flow<List<Podcast>> = podcasts
    override val favoriteEpisodes: Flow<List<Episode>> = episodes
    override val favoritePodcastIds: Flow<Set<String>> = podcasts.map { list -> list.map { it.id }.toSet() }
    override val favoriteEpisodeIds: Flow<Set<String>> = episodes.map { list -> list.map { it.id }.toSet() }

    override suspend fun setPodcastFavorite(podcast: Podcast, favorite: Boolean) {
        if (favorite) putPodcast(podcast, ++clock) else removePodcast(podcast.id)
    }

    override suspend fun setEpisodeFavorite(episode: Episode, favorite: Boolean) {
        if (favorite) putEpisode(episode, ++clock) else removeEpisode(episode.id)
    }

    override suspend fun removePodcast(id: String): RemovedFavorite.PodcastItem? {
        val podcast = podcasts.value.firstOrNull { it.id == id } ?: return null
        podcasts.value = podcasts.value - podcast
        return RemovedFavorite.PodcastItem(podcast, addedAt.remove("p:$id")!!)
    }

    override suspend fun removeEpisode(id: String): RemovedFavorite.EpisodeItem? {
        val episode = episodes.value.firstOrNull { it.id == id } ?: return null
        episodes.value = episodes.value - episode
        return RemovedFavorite.EpisodeItem(episode, addedAt.remove("e:$id")!!)
    }

    override suspend fun restore(removed: RemovedFavorite) {
        when (removed) {
            is RemovedFavorite.PodcastItem -> putPodcast(removed.podcast, removed.addedAtMs)
            is RemovedFavorite.EpisodeItem -> putEpisode(removed.episode, removed.addedAtMs)
        }
    }

    private fun putPodcast(podcast: Podcast, at: Long) {
        addedAt["p:${podcast.id}"] = at
        podcasts.value = (podcasts.value.filterNot { it.id == podcast.id } + podcast)
            .sortedByDescending { addedAt.getValue("p:${it.id}") }
    }

    private fun putEpisode(episode: Episode, at: Long) {
        addedAt["e:${episode.id}"] = at
        episodes.value = (episodes.value.filterNot { it.id == episode.id } + episode)
            .sortedByDescending { addedAt.getValue("e:${it.id}") }
    }
}
