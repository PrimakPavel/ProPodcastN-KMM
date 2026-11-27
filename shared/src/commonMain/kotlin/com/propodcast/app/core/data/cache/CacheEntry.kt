package com.propodcast.app.core.data.cache

import kotlinx.serialization.Serializable

/** What the cache stores per key: the value, when it was saved and which API it came from. */
@Serializable
internal data class CacheEntry<T>(
    val source: String,
    val savedAtMs: Long,
    val value: T,
)
