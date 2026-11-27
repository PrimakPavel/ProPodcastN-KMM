package com.propodcast.app.core.network.dto.response

import com.propodcast.app.core.network.dto.NetworkPodcast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BestPodcastsResponse(
    val podcasts: List<NetworkPodcast> = emptyList(),
    @SerialName("has_next") val hasNext: Boolean = false,
    @SerialName("next_page_number") val nextPageNumber: Int? = null,
    @SerialName("page_number") val pageNumber: Int = 1,
    val total: Int = 0,
)
