package com.propodcast.app.core.network

import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.testing.ListenNotesTestClient
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

class ApiQuotaPluginTest {

    private val server = ListenNotesTestClient()

    private suspend fun call(code: Int, vararg headers: Pair<String, String>) {
        server.enqueue(code, """{"languages":[]}""", *headers)
        apiCall { server.api.languages() }
    }

    @Test
    fun successfulResponse_reportsQuota() = runTest {
        // Values as sent by listen-api-test.listennotes.com.
        call(
            200,
            "X-ListenAPI-Usage" to "1024",
            "X-ListenAPI-FreeQuota" to "2500",
            "X-ListenAPI-NextBillingDate" to "2026-12-11T18:53:16.385542+00:00",
            "X-ListenAPI-Plan" to "FREE",
        )

        assertEquals(
            ApiQuota(
                used = 1024,
                freeQuota = 2500,
                nextBillingDate = Instant.parse("2026-12-11T18:53:16.385542Z"),
                plan = "FREE",
            ),
            server.quotas.single(),
        )
    }

    @Test
    fun errorResponse_isIgnored() = runTest {
        call(429, "X-ListenAPI-Usage" to "2500", "X-ListenAPI-FreeQuota" to "2500")

        assertEquals(emptyList(), server.quotas)
    }

    @Test
    fun missingHeaders_isIgnored() = runTest {
        call(200)
        call(200, "X-ListenAPI-Usage" to "10")

        assertEquals(emptyList(), server.quotas)
    }

    @Test
    fun parse_badDateAndMissingPlan_keepNumbers() {
        val quota = ApiQuotaHeaders.parse(
            headersOf(
                "X-ListenAPI-Usage" to listOf(" 5 "),
                "X-ListenAPI-FreeQuota" to listOf("300"),
                "X-ListenAPI-NextBillingDate" to listOf("not a date"),
            ),
        )!!

        assertEquals(5, quota.used)
        assertEquals(300, quota.freeQuota)
        assertNull(quota.nextBillingDate)
        assertNull(quota.plan)
    }

    @Test
    fun parse_nonNumericUsage_isNull() {
        assertNull(
            ApiQuotaHeaders.parse(
                headersOf("X-ListenAPI-Usage" to listOf("n/a"), "X-ListenAPI-FreeQuota" to listOf("300")),
            ),
        )
    }
}
