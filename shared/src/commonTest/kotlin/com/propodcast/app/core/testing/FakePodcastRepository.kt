package com.propodcast.app.core.testing

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.PodcastDetails
import com.propodcast.app.core.model.Region
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** In-memory [PodcastRepository]; records best-podcast requests and returns configurable results. */
class FakePodcastRepository : PodcastRepository {
    val bestPodcastRequests = mutableListOf<Pair<Int?, String?>>()
    var bestPodcastsResult: (genreId: Int?, region: String?) -> List<Podcast> = { _, _ -> emptyList() }
    var genresResult: Result<List<Genre>> = Result.success(emptyList())
    var regionsResult: Result<List<Region>> = Result.success(emptyList())
    val detailsRequests = mutableListOf<Pair<String, Long?>>()
    var detailsResult: (id: String, nextEpisodePubDate: Long?) -> Result<PodcastDetails> =
        { _, _ -> Result.failure(NotImplementedError()) }
    var recommendationsResult: Result<List<Podcast>> = Result.success(emptyList())

    override fun bestPodcasts(genreId: Int?, region: String?): Flow<PagingData<Podcast>> {
        bestPodcastRequests += genreId to region
        // Complete load states, so Paging test helpers (asSnapshot) see a finished list.
        return flowOf(PagingData.from(bestPodcastsResult(genreId, region), sourceLoadStates = COMPLETE))
    }

    val searchRequests = mutableListOf<Pair<String, String?>>()
    var searchResult: (query: String, language: String?) -> List<Podcast> = { _, _ -> emptyList() }
    var languagesResult: Result<List<String>> = Result.success(emptyList())

    override fun searchPodcasts(query: String, language: String?): Flow<PagingData<Podcast>> {
        searchRequests += query to language
        return flowOf(PagingData.from(searchResult(query, language), sourceLoadStates = COMPLETE))
    }

    override suspend fun podcastDetails(id: String, nextEpisodePubDate: Long?): Result<PodcastDetails> {
        detailsRequests += id to nextEpisodePubDate
        return detailsResult(id, nextEpisodePubDate)
    }

    override suspend fun recommendations(id: String): Result<List<Podcast>> = recommendationsResult

    override suspend fun genres(): Result<List<Genre>> = genresResult

    override suspend fun regions(): Result<List<Region>> = regionsResult

    override suspend fun languages(): Result<List<String>> = languagesResult

    private companion object {
        val COMPLETE = LoadState.NotLoading(endOfPaginationReached = true).let { LoadStates(it, it, it) }
    }
}
