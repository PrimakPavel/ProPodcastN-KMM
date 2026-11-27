package com.propodcast.app.core.data.cache

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.AppException
import com.propodcast.app.core.testing.TestClock
import com.propodcast.app.core.testing.testDataStore
import com.propodcast.app.core.testing.testReferenceDataCache
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.AfterTest
import kotlin.test.Test
import com.propodcast.app.core.testing.TestDirectory

class DataStoreReferenceDataCacheTest {

    private val tmp = TestDirectory()

    @AfterTest
    fun deleteTempFiles() = tmp.delete()

    private val clock = TestClock(nowMs = 1_000)
    private val serializer = ListSerializer(String.serializer())
    private var fetches = 0

    private fun fetchReturning(vararg values: String): suspend () -> Result<List<String>> = {
        fetches++
        Result.success(values.toList())
    }

    private val failingFetch: suspend () -> Result<List<String>> = {
        fetches++
        Result.failure(AppException(AppError.Network))
    }

    private fun TestScope.cache(baseUrl: String = "https://example.com/api/v2/") =
        testReferenceDataCache(testDataStore(tmp.root, backgroundScope), clock, baseUrl)

    @Test
    fun secondCall_withinMaxAge_usesCache() = runTest(UnconfinedTestDispatcher()) {
        val cache = cache()
        assertEquals(listOf("a"), cache.getOrFetch("k", serializer, maxAgeMs = 100, fetch = fetchReturning("a")).getOrThrow())

        clock.nowMs += 99
        assertEquals(listOf("a"), cache.getOrFetch("k", serializer, maxAgeMs = 100, fetch = fetchReturning("b")).getOrThrow())
        assertEquals(1, fetches)
    }

    @Test
    fun expiredEntry_isRefetched() = runTest(UnconfinedTestDispatcher()) {
        val cache = cache()
        cache.getOrFetch("k", serializer, maxAgeMs = 100, fetch = fetchReturning("a"))

        clock.nowMs += 100
        assertEquals(listOf("b"), cache.getOrFetch("k", serializer, maxAgeMs = 100, fetch = fetchReturning("b")).getOrThrow())
        clock.nowMs += 50
        assertEquals(listOf("b"), cache.getOrFetch("k", serializer, 100, fetchReturning("c")).getOrThrow(), "fresh again after refetch")
        assertEquals(2, fetches)
    }

    @Test
    fun expiredEntry_andFetchFails_returnsStaleValue() = runTest(UnconfinedTestDispatcher()) {
        val cache = cache()
        cache.getOrFetch("k", serializer, maxAgeMs = 100, fetch = fetchReturning("a"))
        clock.nowMs += 1_000

        assertEquals(listOf("a"), cache.getOrFetch("k", serializer, maxAgeMs = 100, fetch = failingFetch).getOrThrow())
    }

    @Test
    fun noEntry_andFetchFails_isFailure_andNothingCached() = runTest(UnconfinedTestDispatcher()) {
        val cache = cache()
        val result = cache.getOrFetch("k", serializer, fetch = failingFetch)

        assertEquals(AppError.Network, (result.exceptionOrNull() as AppException).error)
        assertEquals(listOf("a"), cache.getOrFetch("k", serializer, fetch = fetchReturning("a")).getOrThrow())
        assertEquals(2, fetches)
    }

    @Test
    fun entryFromAnotherApi_isIgnored() = runTest(UnconfinedTestDispatcher()) {
        val dataStore = testDataStore(tmp.root, backgroundScope)
        testReferenceDataCache(dataStore, clock, baseUrl = "https://mock/")
            .getOrFetch("k", serializer, fetch = fetchReturning("mock"))

        // Same file, different API (debug switched from the mock server to production).
        val production = testReferenceDataCache(dataStore, clock, baseUrl = "https://production/")
        assertEquals(listOf("real"), production.getOrFetch("k", serializer, fetch = fetchReturning("real")).getOrThrow())
        assertEquals(2, fetches)
    }

    @Test
    fun unreadableEntry_isRefetched() = runTest(UnconfinedTestDispatcher()) {
        val dataStore = testDataStore(tmp.root, backgroundScope)
        dataStore.edit { it[stringPreferencesKey("k")] = "{not json" }
        val cache = testReferenceDataCache(dataStore, clock)

        assertTrue(cache.getOrFetch("k", serializer, fetch = fetchReturning("a")).isSuccess)
        assertEquals(1, fetches)
    }
}
