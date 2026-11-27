package com.propodcast.app.core.model

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class ApiQuotaTest {

    private fun quota(used: Int, freeQuota: Int = 100, plan: String? = "FREE") =
        ApiQuota(used = used, freeQuota = freeQuota, nextBillingDate = null, plan = plan)

    @Test
    fun runningLow_fromEightyPercentOnFreePlan() {
        assertFalse(quota(used = 79).isRunningLow())
        assertTrue(quota(used = 80).isRunningLow())
        assertFalse(quota(used = 99).isExhausted)
        assertTrue(quota(used = 100).isExhausted)
    }

    @Test
    fun remaining_neverNegative() {
        assertEquals(30, quota(used = 70).remaining)
        assertEquals(0, quota(used = 150).remaining)
    }

    @Test
    fun paidPlan_isNeverRunningLow() {
        // PRO / ENTERPRISE are billed for extra requests instead of being blocked.
        assertFalse(quota(used = 6000, freeQuota = 5000, plan = "PRO").isRunningLow())
        assertFalse(quota(used = 6000, freeQuota = 5000, plan = "PRO").isExhausted)
    }

    @Test
    fun unknownPlan_isTreatedAsFree() {
        assertTrue(quota(used = 90, plan = null).isRunningLow())
    }

    @Test
    fun zeroQuota_isNotRunningLow() {
        assertFalse(quota(used = 0, freeQuota = 0).isRunningLow())
    }
}
