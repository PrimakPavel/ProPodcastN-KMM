package com.propodcast.app.core.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.propodcast.app.core.data.cache.DataStoreReferenceDataCache
import com.propodcast.app.core.network.ApiConfig
import kotlinx.coroutines.CoroutineScope
import okio.Path

/** One DataStore per file: DataStore forbids two active instances on the same file. */
fun testDataStore(dir: Path, scope: CoroutineScope, fileName: String = "cache.preferences_pb"): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { dir / fileName })

/** A real [DataStoreReferenceDataCache] on top of [dataStore]. */
fun testReferenceDataCache(
    dataStore: DataStore<Preferences>,
    clock: TestClock = TestClock(),
    baseUrl: String = "https://example.com/api/v2/",
) = DataStoreReferenceDataCache(
    dataStore = dataStore,
    json = testJson,
    apiConfig = ApiConfig(baseUrl = baseUrl, apiKey = ""),
    clock = clock,
)
