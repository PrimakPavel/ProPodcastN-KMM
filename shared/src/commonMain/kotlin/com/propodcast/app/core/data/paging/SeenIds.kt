package com.propodcast.app.core.data.paging

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Remembers item ids already emitted by one PagingSource instance and drops repeats.
 *
 * Listen Notes may return the same podcast on two pages (ranking changes between requests),
 * and the mock server returns the same page for every request. Duplicate ids would crash
 * LazyColumn keys. A new PagingSource (refresh) starts with an empty set.
 * Thread-safe: prepend and append loads may run concurrently (compare-and-set on an immutable set).
 */
@OptIn(ExperimentalAtomicApi::class)
internal class SeenIds<T>(private val idOf: (T) -> String) {
    private val seen = AtomicReference(emptySet<String>())

    /** Items whose id was not seen before, in order; also drops repeats within [items]. */
    fun keepNew(items: List<T>): List<T> {
        while (true) {
            val before = seen.load()
            val added = LinkedHashSet<String>()
            val fresh = items.filter { val id = idOf(it); id !in before && added.add(id) }
            if (seen.compareAndSet(before, before + added)) return fresh
        }
    }
}
