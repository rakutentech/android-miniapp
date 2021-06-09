package com.rakuten.tech.mobile.miniapp.analytics

import com.rakuten.tech.mobile.miniapp.*
import org.amshove.kluent.shouldBeEqualTo
import org.json.JSONObject
import org.junit.Test

class MiniAppAnalyticsSpec {
    // setup MiniApp info.
    private val miniAppInfo = MiniAppInfo(
        id = "test_id",
        displayName = "test_mini_app",
        icon = "test_icon",
        version = Version("test_version_tag", "test_version_id")
    )

    private val cp = JSONObject()
        .put("mini_app_project_id", TEST_HA_ID_PROJECT)
        .put("mini_app_sdk_version", BuildConfig.VERSION_NAME)
        .put("mini_app_id", miniAppInfo.id)
        .put("mini_app_version_id", miniAppInfo.version.versionId)

    private val testParam = mapOf<String, Any>(
        "acc" to TEST_CONFIG1.acc,
        "aid" to TEST_CONFIG1.aid,
        "actype" to Actype.OPEN.value,
        "cp" to cp
    )

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
