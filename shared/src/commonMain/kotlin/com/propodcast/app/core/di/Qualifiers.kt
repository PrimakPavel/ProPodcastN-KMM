package com.propodcast.app.core.di

import org.koin.core.qualifier.named

/** The two Preferences DataStores: user settings and the reference data cache (separate files). */
object DataStoreQualifiers {
    val UserPreferences = named("userPreferencesDataStore")
    val ReferenceCache = named("referenceCacheDataStore")
}
