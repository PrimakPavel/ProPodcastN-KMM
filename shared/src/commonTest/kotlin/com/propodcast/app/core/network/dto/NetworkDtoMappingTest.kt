package com.propodcast.app.core.network.dto

import com.propodcast.app.core.network.dto.response.RegionsResponse
import com.propodcast.app.core.network.dto.response.toModels
import com.propodcast.app.core.testing.testJson
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class NetworkDtoMappingTest {

    @Test
    fun podcast_blankStringsBecomeNull() {
        val podcast = NetworkPodcast(
            id = "p",
            image = "",
            thumbnail = " ",
            website = "",
            listennotesUrl = "",
            language = "",
            country = "",
        ).toModel()

        assertNull(podcast.imageUrl)
        assertNull(podcast.thumbnailUrl)
        assertNull(podcast.website)
        assertNull(podcast.listennotesUrl)
        assertNull(podcast.language)
        assertNull(podcast.country)
    }

    @Test
    fun podcast_imageAndThumbnailFallBackToEachOther() {
        val onlyImage = NetworkPodcast(id = "p", image = IMAGE, thumbnail = "").toModel()
        assertEquals(IMAGE, onlyImage.imageUrl)
        assertEquals(IMAGE, onlyImage.thumbnailUrl)

        val onlyThumb = NetworkPodcast(id = "p", image = null, thumbnail = THUMB).toModel()
        assertEquals(THUMB, onlyThumb.imageUrl)
        assertEquals(THUMB, onlyThumb.thumbnailUrl)
    }

    @Test
    fun searchPodcast_blankStringsAndFallback() {
        val podcast = NetworkSearchPodcast(id = "s", image = "", thumbnail = THUMB, listennotesUrl = "").toModel()

        assertEquals(THUMB, podcast.imageUrl)
        assertEquals(THUMB, podcast.thumbnailUrl)
        assertNull(podcast.listennotesUrl)
    }

    @Test
    fun details_episodesWithoutArtworkUsePodcastArtwork() {
        val details = NetworkPodcast(
            id = "p",
            title = "Podcast",
            image = IMAGE,
            thumbnail = THUMB,
            episodes = listOf(
                NetworkEpisode(id = "own", image = "https://example.com/own.jpg", thumbnail = ""),
                NetworkEpisode(id = "none", image = "", thumbnail = null, audio = ""),
            ),
            nextEpisodePubDate = 10L,
        ).toDetails()

        val (own, none) = details.episodes
        assertEquals("https://example.com/own.jpg", own.imageUrl)
        assertEquals("https://example.com/own.jpg", own.thumbnailUrl)
        assertEquals(IMAGE, none.imageUrl)
        assertEquals(THUMB, none.thumbnailUrl)
        assertNull(none.audioUrl, "blank audio becomes null")
        details.episodes.forEach {
            assertEquals("p", it.podcastId)
            assertEquals("Podcast", it.podcastTitle)
        }
        assertEquals(10L, details.nextEpisodePubDate)
    }

    @Test
    fun podcastWithoutEpisodesField_parsesAsEmptyDetails() {
        // best_podcasts / recommendations items have no episodes or next_episode_pub_date.
        val podcast = testJson.decodeFromString<NetworkPodcast>("""{"id":"p","title":"T"}""")

        val details = podcast.toDetails()

        assertEquals(emptyList<Any>(), details.episodes)
        assertNull(details.nextEpisodePubDate)
    }

    @Test
    fun regions_mapperKeepsApiOrder() {
        val regions = RegionsResponse(mapOf("us" to "United States", "al" to "Albania")).toModels()

        assertEquals(listOf("us", "al"), regions.map { it.code })
    }

    private companion object {
        const val IMAGE = "https://example.com/image.jpg"
        const val THUMB = "https://example.com/thumb.jpg"
    }
}
