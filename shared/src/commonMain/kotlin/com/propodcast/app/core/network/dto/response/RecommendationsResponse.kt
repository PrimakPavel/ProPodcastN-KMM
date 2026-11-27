package com.propodcast.app.core.network.dto.response

import com.propodcast.app.core.network.dto.NetworkPodcast
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationsResponse(val recommendations: List<NetworkPodcast> = emptyList())
