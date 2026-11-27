package com.propodcast.app.core.network

import com.propodcast.app.BuildKonfig

/** Listen Notes API settings. */
data class ApiConfig(
    val baseUrl: String,
    val apiKey: String,
) {
    companion object {
        /** From local.properties through BuildKonfig: mock server by default, production API on request. */
        fun fromBuildKonfig() = ApiConfig(
            baseUrl = BuildKonfig.LISTEN_NOTES_BASE_URL,
            apiKey = BuildKonfig.LISTEN_NOTES_API_KEY,
        )
    }
}
