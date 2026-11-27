package com.propodcast.app.core.testing

import com.propodcast.app.core.data.repository.ApiQuotaRepository
import com.propodcast.app.core.model.ApiQuota
import kotlinx.coroutines.flow.MutableStateFlow

class FakeApiQuotaRepository : ApiQuotaRepository {
    override val quota = MutableStateFlow<ApiQuota?>(null)
}
