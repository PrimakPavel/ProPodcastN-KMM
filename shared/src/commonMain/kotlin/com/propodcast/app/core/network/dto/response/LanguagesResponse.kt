package com.propodcast.app.core.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class LanguagesResponse(val languages: List<String> = emptyList())
