package com.propodcast.app.core.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.AppException
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.network.dto.NetworkPodcast
import com.propodcast.app.core.network.dto.NetworkSearchPodcast
import com.propodcast.app.core.network.dto.response.BestPodcastsResponse
import com.propodcast.app.core.network.dto.response.SearchPodcastsResponse
import com.propodcast.app.core.testing.ListenNotesTestClient
import com.propodcast.app.core.testing.fixture
import com.propodcast.app.core.testing.testJson
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class PagingSourcesTest {

    private val server = ListenNotesTestClient()

    private fun bestPager(genreId: Int? = null, region: String? = null) = TestPager(
        PagingConfig(pageSize = BestPodcastsPagingSource.PAGE_SIZE, enablePlaceholders = false),
        BestPodcastsPagingSource(server.api, genreId, region),
    )

    private fun searchPager() = TestPager(
        PagingConfig(pageSize = SearchPodcastsPagingSource.PAGE_SIZE, enablePlaceholders = false),
        SearchPodcastsPagingSource(server.api, query = "star", language = null),
    )

    private fun enqueueBest(ids: List<String>, hasNext: Boolean = true, nextPageNumber: Int? = null) =
        server.enqueue(
            200,
            testJson.encodeToString(
                BestPodcastsResponse(
                    podcasts = ids.map { NetworkPodcast(id = it, title = it) },
                    hasNext = hasNext,
                    nextPageNumber = nextPageNumber,
                ),
            ),
        )

    private fun enqueueSearch(ids: List<String>, nextOffset: Int?, total: Int = 100) =
        server.enqueue(
            200,
            testJson.encodeToString(
                SearchPodcastsResponse(
                    results = ids.map { NetworkSearchPodcast(id = it, title = it) },
                    total = total,
                    nextOffset = nextOffset,
                ),
            ),
        )

    private val LoadResult<Int, Podcast>?.ids get() = (this as LoadResult.Page).data.map { it.id }

    @Test
    fun bestPodcasts_firstPage_passesFiltersAndNextKeyIsPagePlusOne() = runTest {
        server.enqueueFixture("best_podcasts")
        val pager = bestPager(genreId = 93, region = "us")

        val page = pager.refresh() as LoadResult.Page

        assertEquals(2, page.data.size)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
        val url = server.takeRequest().url
        assertEquals("93", url.parameters["genre_id"])
        assertEquals("us", url.parameters["region"])
        assertEquals("1", url.parameters["page"])
    }

    @Test
    fun bestPodcasts_lastPage_hasNoNextKey() = runTest {
        server.enqueue(200, fixture("best_podcasts").replace("\"has_next\": true", "\"has_next\": false"))

        val page = bestPager().refresh() as LoadResult.Page

        assertNull(page.nextKey)
    }

    @Test
    fun bestPodcasts_ignoresNextPageNumberFromServer() = runTest {
        // Mock server: every page says next_page_number = 3. Using it twice in a row makes Paging throw.
        enqueueBest(listOf("a", "b"), nextPageNumber = 3)
        enqueueBest(listOf("c", "d"), nextPageNumber = 3)
        val pager = bestPager()

        assertEquals(2, (pager.refresh() as LoadResult.Page).nextKey)
        val second = pager.append() as LoadResult.Page

        assertEquals(3, second.nextKey)
        assertEquals("1", server.takeRequest().url.parameters["page"])
        assertEquals("2", server.takeRequest().url.parameters["page"])
    }

    @Test
    fun bestPodcasts_dropsDuplicatesAcrossPages() = runTest {
        enqueueBest(listOf("a", "b", "c"))
        enqueueBest(listOf("c", "d", "a"))
        val pager = bestPager()

        assertEquals(listOf("a", "b", "c"), pager.refresh().ids)
        assertEquals(listOf("d"), pager.append().ids)
    }

    @Test
    fun bestPodcasts_pageWithOnlyDuplicates_endsPaging() = runTest {
        // Mock server returns the same page for every page number.
        enqueueBest(listOf("a", "b"))
        enqueueBest(listOf("a", "b"))
        val pager = bestPager()

        pager.refresh()
        val second = pager.append() as LoadResult.Page

        assertEquals(emptyList<Podcast>(), second.data)
        assertNull(second.nextKey)
    }

    @Test
    fun bestPodcasts_httpError_isAppException() = runTest {
        server.enqueue(429)

        val error = bestPager().refresh() as LoadResult.Error

        assertEquals(AppError.TooManyRequests, (error.throwable as AppException).error)
    }

    @Test
    fun search_nextKeyIsNextOffset_untilTotalReached() = runTest {
        server.enqueueFixture("search") // total 25, next_offset 10
        val pager = searchPager()

        val first = pager.refresh() as LoadResult.Page
        assertEquals(10, first.nextKey)
        assertNull(server.takeRequest().url.parameters["language"])

        enqueueSearch(listOf("other"), nextOffset = 30, total = 25)
        val second = pager.append() as LoadResult.Page
        assertEquals(listOf("other"), second.data.map { it.id })
        assertNull(second.nextKey, "next_offset beyond total ends paging")
        assertEquals("10", server.takeRequest().url.parameters["offset"])
    }

    @Test
    fun search_sameResultsForNextOffset_endsPaging() = runTest {
        // Mock server returns the same 10 results (and next_offset 10) for every offset.
        enqueueSearch(listOf("a", "b"), nextOffset = 10)
        enqueueSearch(listOf("a", "b"), nextOffset = 20)
        val pager = searchPager()

        pager.refresh()
        val second = pager.append() as LoadResult.Page

        assertEquals(emptyList<Podcast>(), second.data)
        assertNull(second.nextKey)
    }

    @Test
    fun search_nextOffsetNotMovingForward_endsPaging() = runTest {
        enqueueSearch(listOf("a"), nextOffset = 10)
        enqueueSearch(listOf("b"), nextOffset = 10)
        val pager = searchPager()

        pager.refresh()
        val second = pager.append() as LoadResult.Page

        assertEquals(listOf("b"), second.data.map { it.id })
        assertNull(second.nextKey)
    }
}
