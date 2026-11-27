package com.propodcast.app.core.di

import com.propodcast.app.BuildKonfig
import com.propodcast.app.core.data.repository.ApiQuotaRepository
import com.propodcast.app.core.data.repository.InMemoryApiQuotaRepository
import com.propodcast.app.core.network.ApiConfig
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.createListenNotesHttpClient
import com.propodcast.app.core.network.listenNotesJson
import org.koin.dsl.bind
import org.koin.dsl.module

/** Listen Notes API: config from BuildKonfig, one HTTP client that reports quota to [ApiQuotaRepository]. */
val networkModule = module {
    single { ApiConfig.fromBuildKonfig() }
    single { listenNotesJson }
    single { InMemoryApiQuotaRepository() } bind ApiQuotaRepository::class
    single {
        val quota = get<InMemoryApiQuotaRepository>()
        createListenNotesHttpClient(config = get(), onQuota = quota::update, logging = BuildKonfig.HTTP_LOGGING)
    }
    single { ListenNotesApi(get()) }
}
