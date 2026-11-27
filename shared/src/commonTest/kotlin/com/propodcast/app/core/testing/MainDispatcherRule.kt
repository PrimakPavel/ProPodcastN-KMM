package com.propodcast.app.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Replaces Dispatchers.Main (used by viewModelScope) with a test dispatcher. Common tests have no JUnit rules:
 * call [start] from `@BeforeTest` and [finish] from `@AfterTest`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) {
    fun start() = Dispatchers.setMain(dispatcher)
    fun finish() = Dispatchers.resetMain()
}
