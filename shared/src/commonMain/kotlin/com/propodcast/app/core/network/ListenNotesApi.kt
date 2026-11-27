package com.propodcast.app.core.network

import com.propodcast.app.core.network.dto.NetworkPodcast
import com.propodcast.app.core.network.dto.response.BestPodcastsResponse
import com.propodcast.app.core.network.dto.response.GenresResponse
import com.propodcast.app.core.network.dto.response.LanguagesResponse
import com.propodcast.app.core.network.dto.response.RecommendationsResponse
import com.propodcast.app.core.network.dto.response.RegionsResponse
import com.propodcast.app.core.network.dto.response.SearchPodcastsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.encodeURLPathPart

/**
 * Listen Notes API v2 over a Ktor [client] made by [createListenNotesHttpClient]: it adds the base URL
 * and the API key header. Null query params are omitted.
 */
class ListenNotesApi(private val client: HttpClient) {

    suspend fun genres(topLevelOnly: Int? = null): GenresResponse =
        client.get("genres") { parameter("top_level_only", topLevelOnly) }.body()

    suspend fun regions(): RegionsResponse = client.get("regions").body()

    suspend fun languages(): LanguagesResponse = client.get("languages").body()

    suspend fun bestPodcasts(
        genreId: Int? = null,
        page: Int = 1,
        region: String? = null,
    ): BestPodcastsResponse = client.get("best_podcasts") {
        parameter("genre_id", genreId)
        parameter("page", page)
        parameter("region", region)
    }.body()

    suspend fun podcast(
        id: String,
        nextEpisodePubDate: Long? = null,
        sort: String = SORT_RECENT_FIRST,
    ): NetworkPodcast = client.get("podcasts/${id.encodeURLPathPart()}") {
        parameter("next_episode_pub_date", nextEpisodePubDate)
        parameter("sort", sort)
    }.body()

    suspend fun recommendations(id: String): RecommendationsResponse =
        client.get("podcasts/${id.encodeURLPathPart()}/recommendations").body()

    suspend fun searchPodcasts(
        query: String,
        offset: Int = 0,
        language: String? = null,
        type: String = TYPE_PODCAST,
    ): SearchPodcastsResponse = client.get("search") {
        parameter("q", query)
        parameter("offset", offset)
        parameter("language", language)
        parameter("type", type)
    }.body()

    companion object {
        const val SORT_RECENT_FIRST = "recent_first"
        const val TYPE_PODCAST = "podcast"

        /** Region used by `best_podcasts` when none is passed (per the API docs). */
        const val DEFAULT_REGION = "us"

        /** The "no language filter" entry returned by `GET languages`. */
        const val ANY_LANGUAGE = "Any language"
    }
}
