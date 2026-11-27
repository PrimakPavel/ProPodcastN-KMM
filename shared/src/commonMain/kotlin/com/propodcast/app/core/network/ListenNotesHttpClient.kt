package com.propodcast.app.core.network

import com.propodcast.app.core.model.ApiQuota
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Lenient JSON for the Listen Notes responses. */
val listenNotesJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

/**
 * Ktor client for [ListenNotesApi]: base URL, API key header, quota tracking, JSON, timeouts.
 * Non-2xx responses throw `ResponseException` (as Retrofit threw `HttpException`).
 *
 * [engine] is for tests (MockEngine); null picks the platform engine: OkHttp on Android, Darwin on iOS.
 */
fun createListenNotesHttpClient(
    config: ApiConfig,
    onQuota: (ApiQuota) -> Unit = {},
    logging: Boolean = false,
    engine: HttpClientEngine? = null,
): HttpClient {
    val setup: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        defaultRequest { url(config.baseUrl) }
        install(ContentNegotiation) { json(listenNotesJson) }
        install(HttpTimeout) {
            connectTimeoutMillis = TIMEOUT_MS
            requestTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis = TIMEOUT_MS
        }
        install(ApiKeyPlugin) { apiKey = config.apiKey }
        install(ApiQuotaPlugin) { this.onQuota = onQuota }
        if (logging) {
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.INFO
                sanitizeHeader { it == API_KEY_HEADER }
            }
        }
    }
    return if (engine != null) HttpClient(engine, setup) else HttpClient(setup)
}

private const val TIMEOUT_MS = 30_000L
