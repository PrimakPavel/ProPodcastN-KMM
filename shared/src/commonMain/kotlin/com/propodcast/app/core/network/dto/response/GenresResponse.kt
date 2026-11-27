package com.propodcast.app.core.network.dto.response

import com.propodcast.app.core.network.dto.NetworkGenre
import kotlinx.serialization.Serializable

@Serializable
data class GenresResponse(val genres: List<NetworkGenre> = emptyList())
