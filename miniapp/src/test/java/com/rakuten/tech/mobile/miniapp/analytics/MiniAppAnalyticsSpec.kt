package com.rakuten.tech.mobile.miniapp.analytics

import com.rakuten.tech.mobile.miniapp.TEST_HA_ID_PROJECT
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class MiniAppAnalyticsSpec {
    private lateinit var miniAppAnalytics: MiniAppAnalytics
    private lateinit var testMiniAppAnalyticsConfig: MiniAppAnalyticsConfig
    private val TEST_RAT_ACC = 1
    private val TEST_RAT_AID = 2

    @Before
    fun setUp() {
        MiniAppAnalytics.init(TEST_HA_ID_PROJECT)
        miniAppAnalytics = Mockito.spy(MiniAppAnalytics.instance!!)
        testMiniAppAnalyticsConfig = MiniAppAnalyticsConfig(TEST_RAT_ACC,TEST_RAT_AID)
    }

    @Test
    fun `addAnalyticsConfig should add the config to the list`() {
        miniAppAnalytics.addAnalyticsConfig(testMiniAppAnalyticsConfig)
        MiniAppAnalytics.listOfExternalConfig.size shouldBe 1
    }

    @Test
    fun `removeAnalyticsConfig should remove the config from the list`() {
        miniAppAnalytics.addAnalyticsConfig(testMiniAppAnalyticsConfig)
        miniAppAnalytics.removeAnalyticsConfig(testMiniAppAnalyticsConfig)

        MiniAppAnalytics.listOfExternalConfig.size shouldBe 0
    }
}
