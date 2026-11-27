package com.propodcast.app.core.network

import io.ktor.client.plugins.api.createClientPlugin

const val API_KEY_HEADER = "X-ListenAPI-Key"

class ApiKeyPluginConfig {
    var apiKey: String = ""
}

/** Adds `X-ListenAPI-Key` when a key is configured (the mock server needs none). */
val ApiKeyPlugin = createClientPlugin("ApiKeyPlugin", ::ApiKeyPluginConfig) {
    val apiKey = pluginConfig.apiKey
    onRequest { request, _ ->
        if (apiKey.isNotBlank()) request.headers[API_KEY_HEADER] = apiKey
    }
}
