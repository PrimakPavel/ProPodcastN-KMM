package com.propodcast.app.core.model

import com.propodcast.app.core.testing.testEpisode
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class NowPlayingTest {

    @Test
    fun episode_toNowPlaying_usesAudioDurationAndImageFallback() {
        val episode = testEpisode("e1").copy(durationSec = 90, imageUrl = null, thumbnailUrl = "thumb")

        val item = episode.toNowPlaying()!!

        assertEquals("e1", item.episodeId)
        assertEquals("https://example.com/e1.mp3", item.audioUrl)
        assertEquals("Podcast p1", item.podcastTitle)
        assertEquals("thumb", item.imageUrl)
        assertEquals(90_000L, item.durationMs)
    }

    @Test
    fun episodeWithoutAudio_isNotPlayable() {
        assertNull(testEpisode("e1").copy(audioUrl = null).toNowPlaying())
    }

    @Test
    fun lastPlayed_roundTrip_keepsEverythingButPosition() {
        val item = testEpisode("e1").toNowPlaying()!!

        val last = item.toLastPlayed(positionMs = 42_000)

        assertEquals(42_000L, last.positionMs)
        assertEquals(item, last.toNowPlaying())
    }
}
