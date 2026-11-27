package com.propodcast.app.feature.network

import com.propodcast.app.core.connectivity.ConnectionType
import com.propodcast.app.core.testing.FakeConnectivityMonitor
import com.propodcast.app.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class NetworkBannerViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setMainDispatcher() = mainDispatcherRule.start()

    @AfterTest
    fun resetMainDispatcher() = mainDispatcherRule.finish()

    private val connectivity = FakeConnectivityMonitor(ConnectionType.UNMETERED)

    // Lazy: created after MainDispatcherRule has swapped Dispatchers.Main.
    private val viewModel by lazy { NetworkBannerViewModel(connectivity) }

    /** Virtual time of the Main test dispatcher, which runs the ViewModel. */
    private fun test(block: suspend TestScope.() -> Unit) =
        runTest(mainDispatcherRule.dispatcher) { block() }

    @Test
    fun online_atStart_showsNothing() = test {
        assertEquals(NetworkBanner.HIDDEN, viewModel.banner.value)
    }

    @Test
    fun offline_atStart_showsRedRightAway() = test {
        connectivity.connection.value = ConnectionType.NONE
        assertEquals(NetworkBanner.OFFLINE, viewModel.banner.value)
    }

    @Test
    fun outage_thenBackOnline_showsGreenForThreeSeconds() = test {
        viewModel.banner // start
        connectivity.connection.value = ConnectionType.NONE
        assertEquals(NetworkBanner.OFFLINE, viewModel.banner.value)

        connectivity.connection.value = ConnectionType.METERED
        assertEquals(NetworkBanner.BACK_ONLINE, viewModel.banner.value)

        advanceTimeBy(NetworkBannerViewModel.BACK_ONLINE_MS - 1)
        runCurrent()
        assertEquals(NetworkBanner.BACK_ONLINE, viewModel.banner.value)

        advanceTimeBy(2)
        runCurrent()
        assertEquals(NetworkBanner.HIDDEN, viewModel.banner.value)
    }

    @Test
    fun droppingAgain_duringGreen_goesBackToRed() = test {
        viewModel.banner
        connectivity.connection.value = ConnectionType.NONE
        connectivity.connection.value = ConnectionType.UNMETERED
        advanceTimeBy(1_000)

        connectivity.connection.value = ConnectionType.NONE
        assertEquals(NetworkBanner.OFFLINE, viewModel.banner.value)

        advanceTimeBy(NetworkBannerViewModel.BACK_ONLINE_MS * 2)
        runCurrent()
        assertEquals(NetworkBanner.OFFLINE, viewModel.banner.value, "the old green timer does not hide the red strip")
    }

    @Test
    fun wifiToMobile_isNotAnOutage() = test {
        viewModel.banner
        connectivity.connection.value = ConnectionType.METERED
        assertEquals(NetworkBanner.HIDDEN, viewModel.banner.value)
    }
}
