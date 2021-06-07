package com.rakuten.tech.mobile.miniapp.analytics

import com.rakuten.tech.mobile.miniapp.*
import org.amshove.kluent.shouldBeEqualTo
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class MiniAppAnalyticsSpec {
    private val testParam = mutableMapOf<String, Any>()
    private lateinit var miniAppInfo: MiniAppInfo

    @Before
    fun setUp() {
        // setup MiniApp info.
        miniAppInfo = MiniAppInfo(
            id = "test_id",
            displayName = "test_mini_app",
            icon = "test_icon",
            version = Version("test_version_tag", "test_version_id")
        )

        // create the test parameters.
        testParam["acc"] = TEST_CONFIG1.acc
        testParam["aid"] = TEST_CONFIG1.aid
        testParam["actype"] = Actype.OPEN.value

        val cp = JSONObject()
            .put("mini_app_project_id", TEST_HA_ID_PROJECT)
            .put("mini_app_sdk_version", BuildConfig.VERSION_NAME)
            .put("mini_app_id", miniAppInfo.id)
            .put("mini_app_version_id", miniAppInfo.version.versionId)

        testParam["cp"] = cp
    }

    @Test
    fun `createParams will create the parameters to send to analytics with correct format`() {
        val params = MiniAppAnalytics.createParams(
            rasProjectId = TEST_HA_ID_PROJECT,
            acc = TEST_CONFIG1.acc,
            aid = TEST_CONFIG1.aid,
            actype = Actype.OPEN,
            miniAppInfo = miniAppInfo
        )

        params.toString() shouldBeEqualTo testParam.toString()
    }
}
