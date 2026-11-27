package com.propodcast.app.core.testing

import com.propodcast.app.core.database.ProPodcastDatabase

/**
 * Base for tests that need Room. On Android it runs under Robolectric (host JVM has no Android SQLite
 * otherwise); on iOS it is a plain test.
 */
expect abstract class DatabaseTest()

/** A fresh in-memory database. Call from a [DatabaseTest]. */
expect fun inMemoryTestDatabase(): ProPodcastDatabase

/**
 * Base for Compose UI tests (`runComposeUiTest`). On the Android host they need Robolectric (an Activity to host
 * the content); on iOS they run as they are.
 */
expect abstract class UiTestBase()
