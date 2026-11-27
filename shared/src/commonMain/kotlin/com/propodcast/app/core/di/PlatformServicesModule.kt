package com.propodcast.app.core.di

import com.propodcast.app.core.connectivity.ConnectivityMonitor
import com.propodcast.app.core.connectivity.StubConnectivityMonitor
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.data.repository.StubDownloadsRepository
import com.propodcast.app.core.player.PlayerController
import com.propodcast.app.core.player.StubPlayerController
import org.koin.dsl.module

/** Player, connectivity and downloads. Stubs until stage 7 brings the platform implementations. */
val platformServicesModule = module {
    single<PlayerController> { StubPlayerController() }
    single<ConnectivityMonitor> { StubConnectivityMonitor() }
    single<DownloadsRepository> { StubDownloadsRepository() }
}
