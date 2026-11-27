package com.propodcast.app.core.data.repository

import com.propodcast.app.core.model.ApiQuota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Latest known Listen Notes API usage; null until the first successful response in this process. */
interface ApiQuotaRepository {
    val quota: StateFlow<ApiQuota?>
}

/** Kept in memory: every successful response carries fresh values, so nothing is persisted. */
class InMemoryApiQuotaRepository : ApiQuotaRepository {
    private val _quota = MutableStateFlow<ApiQuota?>(null)
    override val quota: StateFlow<ApiQuota?> = _quota.asStateFlow()

    fun update(quota: ApiQuota) {
        _quota.value = quota
    }
}
