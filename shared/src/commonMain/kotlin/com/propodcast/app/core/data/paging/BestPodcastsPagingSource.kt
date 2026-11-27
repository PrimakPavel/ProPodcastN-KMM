package com.propodcast.app.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.dto.toModel

/**
 * `best_podcasts` is paged by page number (1-based) with `has_next`.
 *
 * Keys are computed as page ± 1 rather than taken from `next_page_number`: the mock server
 * always answers `next_page_number = 3`, and Paging 3 throws when two sequential pages
 * return the same key. Paging stops when a page brings no new podcasts.
 */
class BestPodcastsPagingSource(
    private val api: ListenNotesApi,
    private val genreId: Int?,
    private val region: String?,
) : PagingSource<Int, Podcast>() {

    private val seenIds = SeenIds<Podcast> { it.id }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Podcast> {
        val page = params.key ?: FIRST_PAGE
        return apiCall { api.bestPodcasts(genreId = genreId, page = page, region = region) }
            .fold(
                onSuccess = { response ->
                    val podcasts = seenIds.keepNew(response.podcasts.map { it.toModel() })
                    val gotNew = podcasts.isNotEmpty()
                    LoadResult.Page(
                        data = podcasts,
                        prevKey = if (page > FIRST_PAGE && gotNew) page - 1 else null,
                        nextKey = if (response.hasNext && gotNew) page + 1 else null,
                    )
                },
                onFailure = { LoadResult.Error(it) },
            )
    }

    override fun getRefreshKey(state: PagingState<Int, Podcast>): Int? =
        state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }

    companion object {
        const val FIRST_PAGE = 1

        /** Listen Notes returns 20 best podcasts per page. */
        const val PAGE_SIZE = 20
    }
}
