package com.propodcast.app.core.testing

import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.network.ApiConfig
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.createListenNotesHttpClient
import com.propodcast.app.core.network.listenNotesJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/** Same Json setup as the app. */
val testJson = listenNotesJson

fun fixture(name: String): String = ListenNotesFixtures.get(name)

private data class QueuedResponse(val code: Int, val body: String, val headers: List<Pair<String, String>>)

/** Ktor MockEngine + a real [ListenNotesApi] on top of it (replaces MockWebServer). */
class ListenNotesTestClient(apiKey: String = "") {
    private val queue = ArrayDeque<QueuedResponse>()
    val quotas = mutableListOf<ApiQuota>()

    private val engine = MockEngine { _ ->
        val next = queue.removeFirstOrNull() ?: error("No response enqueued")
        val headers = next.headers + (HttpHeaders.ContentType to "application/json")
        respond(
            content = next.body,
            status = HttpStatusCode.fromValue(next.code),
            headers = headersOf(*headers.map { (name, value) -> name to listOf(value) }.toTypedArray()),
        )
    }

    val api = ListenNotesApi(
        createListenNotesHttpClient(
            config = ApiConfig(baseUrl = BASE_URL, apiKey = apiKey),
            onQuota = { quotas += it },
            engine = engine,
        ),
    )

    val requests: List<HttpRequestData> get() = engine.requestHistory

    fun enqueueFixture(name: String) = enqueue(200, fixture(name))

    fun enqueue(code: Int, body: String = "{}", vararg headers: Pair<String, String>) {
        queue.addLast(QueuedResponse(code, body, headers.toList()))
    }

    companion object {
        const val BASE_URL = "https://listen-api-test.listennotes.com/api/v2/"
    }
}
