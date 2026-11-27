package com.propodcast.app.core.data.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.propodcast.app.core.network.ApiConfig
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Keeps rarely changing API lists (genres, regions, languages) on disk so they don't cost quota on every launch.
 */
interface ReferenceDataCache {
    /**
     * Returns the cached value while it is younger than [maxAgeMs], otherwise calls [fetch] and saves its result.
     * If [fetch] fails, an expired value is still returned: stale lists beat an error.
     */
    suspend fun <T> getOrFetch(
        key: String,
        serializer: KSerializer<T>,
        maxAgeMs: Long = DEFAULT_MAX_AGE_MS,
        fetch: suspend () -> Result<T>,
    ): Result<T>

    companion object {
        val DEFAULT_MAX_AGE_MS: Long = 7.days.inWholeMilliseconds
    }
}

/** [dataStore] is the reference cache file, separate from user preferences. */
class DataStoreReferenceDataCache(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
    apiConfig: ApiConfig,
    private val clock: Clock = Clock.System,
) : ReferenceDataCache {

    /** Entries from the mock server and the production API must not mix (debug can switch between them). */
    private val source = apiConfig.baseUrl
    private val log = Logger.withTag("ReferenceDataCache")

    override suspend fun <T> getOrFetch(
        key: String,
        serializer: KSerializer<T>,
        maxAgeMs: Long,
        fetch: suspend () -> Result<T>,
    ): Result<T> {
        val entrySerializer = CacheEntry.serializer(serializer)
        val cached = read(key, entrySerializer)
        if (cached != null && nowMs() - cached.savedAtMs in 0 until maxAgeMs) {
            return Result.success(cached.value)
        }
        return fetch()
            .onSuccess { value -> write(key, entrySerializer, CacheEntry(source, nowMs(), value)) }
            .recoverCatching { error -> cached?.value ?: throw error }
    }

    private fun nowMs() = clock.now().toEpochMilliseconds()

    private suspend fun <T> read(key: String, serializer: KSerializer<CacheEntry<T>>): CacheEntry<T>? {
        val raw = dataStore.data.first()[stringPreferencesKey(key)] ?: return null
        return try {
            json.decodeFromString(serializer, raw).takeIf { it.source == source }
        } catch (e: SerializationException) {
            // Format changed between app versions: treat as missing, the next fetch overwrites it.
            log.w(e) { "Dropping unreadable cache entry $key" }
            null
        } catch (e: IllegalArgumentException) {
            log.w(e) { "Dropping unreadable cache entry $key" }
            null
        }
    }

    private suspend fun <T> write(key: String, serializer: KSerializer<CacheEntry<T>>, entry: CacheEntry<T>) {
        dataStore.edit { it[stringPreferencesKey(key)] = json.encodeToString(serializer, entry) }
    }
}
