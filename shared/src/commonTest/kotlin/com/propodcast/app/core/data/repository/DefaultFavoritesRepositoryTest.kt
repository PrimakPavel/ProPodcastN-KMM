package com.propodcast.app.core.data.repository

import app.cash.turbine.test
import com.propodcast.app.core.analytics.NoOpAnalyticsTracker
import com.propodcast.app.core.database.ProPodcastDatabase
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.testing.DatabaseTest
import com.propodcast.app.core.testing.TestClock
import com.propodcast.app.core.testing.inMemoryTestDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test

class DefaultFavoritesRepositoryTest : DatabaseTest() {

    private lateinit var db: ProPodcastDatabase
    private lateinit var repository: DefaultFavoritesRepository
    private val clock = TestClock(nowMs = 1_000)

    @BeforeTest
    fun setUp() {
        db = inMemoryTestDatabase()
        repository = DefaultFavoritesRepository(db.favoritePodcastDao(), db.favoriteEpisodeDao(), NoOpAnalyticsTracker, clock)
    }

    @AfterTest
    fun tearDown() = db.close()

    @Test
    fun podcasts_addNewestFirst_andRemove() = runTest {
        repository.setPodcastFavorite(podcast("p1"), favorite = true)
        clock.nowMs += 1
        repository.setPodcastFavorite(podcast("p2"), favorite = true)

        repository.favoritePodcasts.test {
            assertEquals(listOf("p2", "p1"), awaitItem().map { it.id })

            repository.setPodcastFavorite(podcast("p2"), favorite = false)
            assertEquals(listOf("p1"), awaitItem().map { it.id })
        }
        repository.favoritePodcastIds.test {
            assertEquals(setOf("p1"), awaitItem())
        }
    }

    @Test
    fun addedAt_comesFromClock_andTiesKeepInsertOrder() = runTest {
        // Same millisecond: rowid keeps "last added first".
        repository.setPodcastFavorite(podcast("p1"), favorite = true)
        repository.setPodcastFavorite(podcast("p2"), favorite = true)
        repository.setPodcastFavorite(podcast("p3"), favorite = true)

        repository.favoritePodcasts.test {
            assertEquals(listOf("p3", "p2", "p1"), awaitItem().map { it.id })
        }
        assertEquals(listOf(1_000L), db.favoritePodcastDao().observeAll().first().map { it.addedAt }.distinct())
    }

    @Test
    fun remove_thenRestore_keepsOriginalPosition() = runTest {
        repository.setPodcastFavorite(podcast("p1"), favorite = true)
        clock.nowMs += 1
        repository.setPodcastFavorite(podcast("p2"), favorite = true)
        clock.nowMs += 1
        repository.setPodcastFavorite(podcast("p3"), favorite = true)

        val removed = repository.removePodcast("p2")!!
        assertEquals(1_001L, removed.addedAtMs)
        assertEquals(null, repository.removePodcast("p2"))

        clock.nowMs += 10 // restore must not take a new date
        repository.restore(removed)
        repository.favoritePodcasts.test {
            assertEquals(listOf("p3", "p2", "p1"), awaitItem().map { it.id })
        }

        repository.setEpisodeFavorite(episode("e1"), favorite = true)
        val removedEpisode = repository.removeEpisode("e1")!!
        repository.favoriteEpisodes.test {
            assertEquals(emptyList<Episode>(), awaitItem())
            repository.restore(removedEpisode)
            assertEquals(listOf(episode("e1")), awaitItem())
        }
    }

    @Test
    fun refavoriting_movesItemToTop() = runTest {
        repository.setEpisodeFavorite(episode("e1"), favorite = true)
        clock.nowMs += 1
        repository.setEpisodeFavorite(episode("e2"), favorite = true)
        clock.nowMs += 1
        repository.setEpisodeFavorite(episode("e1"), favorite = true)

        repository.favoriteEpisodes.test {
            assertEquals(listOf("e1", "e2"), awaitItem().map { it.id })
        }
    }

    @Test
    fun episodes_roundTripKeepsPodcastInfo() = runTest {
        val episode = episode("e1")
        repository.setEpisodeFavorite(episode, favorite = true)
        repository.setEpisodeFavorite(episode, favorite = true) // upsert, no duplicate

        repository.favoriteEpisodes.test {
            assertEquals(listOf(episode), awaitItem())
        }
        repository.favoriteEpisodeIds.test {
            assertEquals(setOf("e1"), awaitItem())
        }
    }

    private fun podcast(id: String) = Podcast(
        id = id,
        title = "Podcast $id",
        publisher = "Publisher",
        imageUrl = "https://example.com/$id.jpg",
        thumbnailUrl = null,
        description = "",
        totalEpisodes = 10,
        listennotesUrl = null,
        latestPubDateMs = 1L,
    )

    private fun episode(id: String) = Episode(
        id = id,
        title = "Episode $id",
        description = "",
        audioUrl = "https://example.com/$id.mp3",
        imageUrl = null,
        thumbnailUrl = null,
        pubDateMs = 1L,
        durationSec = 60,
        listennotesUrl = null,
        podcastId = "p1",
        podcastTitle = "Podcast p1",
    )

}
