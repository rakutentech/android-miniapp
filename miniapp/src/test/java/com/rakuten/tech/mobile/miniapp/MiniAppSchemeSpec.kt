package com.rakuten.tech.mobile.miniapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class MiniAppSchemeSpec {
    private lateinit var context: Context
    private lateinit var schemeWithAppId: MiniAppScheme
    private lateinit var schemeWithCustomUrl: MiniAppScheme

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        schemeWithAppId = MiniAppScheme.schemeWithAppId(TEST_ID_MINIAPP)
        schemeWithCustomUrl = MiniAppScheme.schemeWithCustomUrl(TEST_URL_HTTPS_1)
    }

    @Test
    fun `miniAppDomain with AppId should return the expected value`() {
        schemeWithAppId.miniAppDomain shouldBeEqualTo "mscheme.$TEST_ID_MINIAPP"
        schemeWithAppId.appUrl shouldBeEqualTo null
    }

    @Test
    fun `miniAppCustomScheme with AppId should return the expected value`() {
        schemeWithAppId.miniAppCustomScheme shouldBeEqualTo "mscheme.$TEST_ID_MINIAPP://"
        schemeWithAppId.appUrl shouldBeEqualTo null
    }

    @Test
    fun `miniAppCustomDomain with AppId should return the expected value`() {
        schemeWithAppId.miniAppCustomDomain shouldBeEqualTo "https://mscheme.$TEST_ID_MINIAPP/"
        schemeWithAppId.appUrl shouldBeEqualTo null
    }

    @Test
    fun `miniAppScheme with custom url should return the expected values`() {
        schemeWithCustomUrl.appUrl shouldBeEqualTo TEST_URL_HTTPS_1
        schemeWithCustomUrl.miniAppDomain shouldNotBeEqualTo "mscheme.$TEST_ID_MINIAPP"
        schemeWithCustomUrl.miniAppCustomScheme shouldNotBeEqualTo "mscheme.$TEST_ID_MINIAPP://"
        schemeWithCustomUrl.miniAppCustomDomain shouldNotBeEqualTo "https://mscheme.$TEST_ID_MINIAPP/"
    }

    /** region: isMiniAppUrl */
    @Test
    fun `isMiniAppUrl with AppId should return the expected values`() {
        schemeWithAppId.isMiniAppUrl("mscheme.$TEST_ID_MINIAPP://") shouldBeEqualTo true
        schemeWithAppId.isMiniAppUrl("https://mscheme.$TEST_ID_MINIAPP/") shouldBeEqualTo true
    }

    @Test
    fun `isMiniAppUrl with custom url should return the expected values`() {
        schemeWithCustomUrl.appUrl shouldBeEqualTo TEST_URL_HTTPS_1
        schemeWithCustomUrl.isMiniAppUrl(TEST_URL_HTTPS_1) shouldBeEqualTo true
    }

    @Test
    fun `isMiniAppUrl with invalid url should return the expected value`() {
        schemeWithCustomUrl.isMiniAppUrl("www.example.com") shouldBeEqualTo false
    }
    /** end region */

    /** region: appendParametersToUrl */
    @Test
    fun `appendParametersToUrl should return the url when query is empty`() {
        val expectedUrl = "mscheme.$TEST_ID_MINIAPP://"
        schemeWithAppId.appendParametersToUrl(expectedUrl, "") shouldBeEqualTo expectedUrl
    }

    @Test
    fun `appendParametersToUrl should return the appended url when query is not empty`() {
        val expectedUrl = "mscheme.$TEST_ID_MINIAPP://"
        val expectedQuery = TEST_URL_PARAMS
        schemeWithAppId.appendParametersToUrl(
            expectedUrl,
            expectedQuery
        ) shouldBeEqualTo "$expectedUrl?$expectedQuery"
    }

    /** end region */

    @Test
    fun `resolveParameters should append qus mark prior to the parameters`() {
        val query = "param1=value1&param2=value2"
        schemeWithAppId.resolveParameters(query) shouldBeEqualTo "?$query"
    }

    @Test
    fun `should not start activity when there is no supported activity`() {
        schemeWithAppId.startExportedActivity(Intent("TEST_ACTION"), context) shouldBe false
    }

    @Test
    fun `should start activity when there is supported activity`() {
        val intent: Intent = mock()
        When calling intent.resolveActivity(context.packageManager) itReturns ComponentName("", "")
        When calling intent.flags itReturns Intent.FLAG_ACTIVITY_NEW_TASK
        schemeWithAppId.startExportedActivity(intent, context) shouldBe true
    }
}
