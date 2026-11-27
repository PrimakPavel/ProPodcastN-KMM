package com.propodcast.app.core.network.dto

import com.propodcast.app.core.model.Genre
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkGenre(
    val id: Int,
    val name: String,
    @SerialName("parent_id") val parentId: Int? = null,
)

fun NetworkGenre.toModel() = Genre(id = id, name = name, parentId = parentId)
