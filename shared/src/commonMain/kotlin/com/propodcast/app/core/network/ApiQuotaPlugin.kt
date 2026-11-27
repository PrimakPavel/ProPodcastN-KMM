package com.propodcast.app.core.network

import com.propodcast.app.core.model.ApiQuota
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.Headers
import io.ktor.http.isSuccess
import kotlin.time.Instant

class ApiQuotaPluginConfig {
    var onQuota: (ApiQuota) -> Unit = {}
}

/** Reads the Listen Notes usage headers from successful responses and reports them to `onQuota`. */
val ApiQuotaPlugin = createClientPlugin("ApiQuotaPlugin", ::ApiQuotaPluginConfig) {
    val onQuota = pluginConfig.onQuota
    onResponse { response ->
        if (response.status.isSuccess()) {
            ApiQuotaHeaders.parse(response.headers)?.let(onQuota)
        }
    }
}

object ApiQuotaHeaders {
    const val USAGE = "X-ListenAPI-Usage"
    const val FREE_QUOTA = "X-ListenAPI-FreeQuota"
    const val NEXT_BILLING_DATE = "X-ListenAPI-NextBillingDate"
    const val PLAN = "X-ListenAPI-Plan"

    /** Null when the usage or quota header is missing or not a number. */
    fun parse(headers: Headers): ApiQuota? {
        val used = headers[USAGE]?.trim()?.toIntOrNull() ?: return null
        val freeQuota = headers[FREE_QUOTA]?.trim()?.toIntOrNull() ?: return null
        // ISO 8601 with an offset, e.g. "2026-12-11T18:53:16.385542+00:00".
        val nextBillingDate = headers[NEXT_BILLING_DATE]?.let { Instant.parseOrNull(it.trim()) }
        return ApiQuota(
            used = used,
            freeQuota = freeQuota,
            nextBillingDate = nextBillingDate,
            plan = headers[PLAN]?.trim()?.takeIf { it.isNotEmpty() },
        )
    }
}
