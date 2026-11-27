package com.propodcast.app.core.network.dto.response

import com.propodcast.app.core.model.Region
import kotlinx.serialization.Serializable

/** `regions` is a map of region code → display name, e.g. "us" → "United States". */
@Serializable
data class RegionsResponse(val regions: Map<String, String> = emptyMap())

fun RegionsResponse.toModels(): List<Region> =
    regions.map { (code, name) -> Region(code = code, name = name) }
