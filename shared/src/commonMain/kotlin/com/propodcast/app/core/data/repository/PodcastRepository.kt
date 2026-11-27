package com.propodcast.app.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.propodcast.app.core.analytics.AnalyticsEvents
import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.data.cache.ReferenceDataCache
import com.propodcast.app.core.data.paging.BestPodcastsPagingSource
import com.propodcast.app.core.data.paging.SearchPodcastsPagingSource
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.PodcastDetails
import com.propodcast.app.core.model.Region
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.ListenNotesApi.Companion.ANY_LANGUAGE
import com.propodcast.app.core.network.dto.response.toModels
import com.propodcast.app.core.network.dto.toDetails
import com.propodcast.app.core.network.dto.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/** Remote podcast data. Failures come back as [Result.failure] with an AppException. */
interface PodcastRepository {
    fun bestPodcasts(genreId: Int?, region: String?): Flow<PagingData<Podcast>>
    fun searchPodcasts(query: String, language: String?): Flow<PagingData<Podcast>>

    /** First page when [nextEpisodePubDate] is null, otherwise the next page of episodes. */
    suspend fun podcastDetails(id: String, nextEpisodePubDate: Long? = null): Result<PodcastDetails>
    suspend fun recommendations(id: String): Result<List<Podcast>>
    suspend fun genres(): Result<List<Genre>>
    suspend fun regions(): Result<List<Region>>

    /** Languages for the search filter, without the API's "Any language" entry. */
    suspend fun languages(): Result<List<String>>
}

class DefaultPodcastRepository(
    private val api: ListenNotesApi,
    private val cache: ReferenceDataCache,
    private val analytics: AnalyticsTracker,
) : PodcastRepository {

    override fun bestPodcasts(genreId: Int?, region: String?): Flow<PagingData<Podcast>> =
        Pager(pagingConfig(BestPodcastsPagingSource.PAGE_SIZE)) {
            BestPodcastsPagingSource(api, genreId, region)
        }.flow

    override fun searchPodcasts(query: String, language: String?): Flow<PagingData<Podcast>> {
        // The query itself is user-entered text: not sent.
        val languageFilter = language?.takeUnless { it == ANY_LANGUAGE } != null
        analytics.logEvent(AnalyticsEvents.SEARCH, mapOf(AnalyticsEvents.PARAM_LANGUAGE_FILTER to languageFilter.toString()))
        return Pager(pagingConfig(SearchPodcastsPagingSource.PAGE_SIZE)) {
            SearchPodcastsPagingSource(api, query, language?.takeUnless { it == ANY_LANGUAGE })
        }.flow
    }

    override suspend fun podcastDetails(id: String, nextEpisodePubDate: Long?): Result<PodcastDetails> =
        apiCall { api.podcast(id = id, nextEpisodePubDate = nextEpisodePubDate).toDetails() }

    override suspend fun recommendations(id: String): Result<List<Podcast>> =
        apiCall { api.recommendations(id).recommendations.map { it.toModel() } }

    // Genres, regions and languages rarely change: cached on disk, refreshed weekly.
    override suspend fun genres(): Result<List<Genre>> =
        cache.getOrFetch(CACHE_KEY_GENRES, ListSerializer(Genre.serializer())) {
            apiCall { api.genres().genres.map { it.toModel() }.sortedBy { it.name } }
        }

    override suspend fun regions(): Result<List<Region>> =
        cache.getOrFetch(CACHE_KEY_REGIONS, ListSerializer(Region.serializer())) {
            apiCall { api.regions().toModels().sortedBy { it.name } }
        }

    override suspend fun languages(): Result<List<String>> =
        cache.getOrFetch(CACHE_KEY_LANGUAGES, ListSerializer(String.serializer())) {
            apiCall { api.languages().languages.filterNot { it == ANY_LANGUAGE } }
        }

    /** Page size matches what the API returns per request; Paging only uses it for prefetching. */
    private fun pagingConfig(pageSize: Int) = PagingConfig(
        pageSize = pageSize,
        initialLoadSize = pageSize,
        prefetchDistance = pageSize / 2,
        enablePlaceholders = false,
    )

    private companion object {
        const val CACHE_KEY_GENRES = "genres"
        const val CACHE_KEY_REGIONS = "regions"
        const val CACHE_KEY_LANGUAGES = "languages"
    }
}
