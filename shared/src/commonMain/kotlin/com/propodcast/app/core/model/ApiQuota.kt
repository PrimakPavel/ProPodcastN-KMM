package com.propodcast.app.core.model

import kotlin.time.Instant

/**
 * Listen Notes API usage in the current billing cycle, from the `X-ListenAPI-*` headers
 * of the latest successful response.
 *
 * On the FREE plan requests fail with 429 once [used] reaches [freeQuota]; on paid plans
 * extra requests are billed instead.
 */
data class ApiQuota(
    val used: Int,
    val freeQuota: Int,
    val nextBillingDate: Instant?,
    /** From the undocumented `X-ListenAPI-Plan` header, e.g. "FREE"; null if absent. */
    val plan: String?,
) {
    val remaining: Int
        get() = (freeQuota - used).coerceAtLeast(0)

    /** True when the free plan (or an unknown plan) has used [threshold] of its quota. */
    fun isRunningLow(threshold: Float = DEFAULT_LOW_THRESHOLD): Boolean =
        freeQuota > 0 && isBlockedWhenExceeded && used >= freeQuota * threshold

    val isExhausted: Boolean
        get() = freeQuota > 0 && isBlockedWhenExceeded && used >= freeQuota

    /** Only the FREE plan is blocked after the quota; unknown plan is treated as FREE to be safe. */
    private val isBlockedWhenExceeded: Boolean
        get() = plan == null || plan.equals(FREE_PLAN, ignoreCase = true)

    companion object {
        const val FREE_PLAN = "FREE"
        const val DEFAULT_LOW_THRESHOLD = 0.8f
    }
}
