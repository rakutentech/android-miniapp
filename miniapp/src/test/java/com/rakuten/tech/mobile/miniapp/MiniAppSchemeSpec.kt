package com.rakuten.tech.mobile.miniapp

import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.amshove.kluent.shouldNotEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class MiniAppSchemeSpec {

    private lateinit var schemeWithAppId: MiniAppScheme
    private lateinit var schemeWithCustomUrl: MiniAppScheme

    @Before
    fun setup() {
        schemeWithAppId = MiniAppScheme.schemeWithAppId(TEST_ID_MINIAPP)
        schemeWithCustomUrl = MiniAppScheme.schemeWithCustomUrl(TEST_URL_HTTPS_1)
    }

    @Test
    fun `miniAppDomain with AppId should return the expected value`() {
        schemeWithAppId.miniAppDomain shouldEqual "mscheme.$TEST_ID_MINIAPP"
        schemeWithAppId.appUrl shouldEqual null
    }

    @Test
    fun `miniAppCustomScheme with AppId should return the expected value`() {
        schemeWithAppId.miniAppCustomScheme shouldEqual "mscheme.$TEST_ID_MINIAPP://"
        schemeWithAppId.appUrl shouldEqual null
    }

    @Test
    fun `miniAppCustomDomain with AppId should return the expected value`() {
        schemeWithAppId.miniAppCustomDomain shouldEqual "https://mscheme.$TEST_ID_MINIAPP/"
        schemeWithAppId.appUrl shouldEqual null
    }

    @Test
    fun `miniAppScheme with custom url should return the expected values`() {
        schemeWithCustomUrl.appUrl shouldEqual TEST_URL_HTTPS_1
        schemeWithCustomUrl.miniAppDomain shouldNotEqual "mscheme.$TEST_ID_MINIAPP"
        schemeWithCustomUrl.miniAppCustomScheme shouldNotEqual "mscheme.$TEST_ID_MINIAPP://"
        schemeWithCustomUrl.miniAppCustomDomain shouldNotEqual "https://mscheme.$TEST_ID_MINIAPP/"
    }

    /** region: isMiniAppUrl */
    @Test
    fun `isMiniAppUrl with AppId should return the expected values`() {
        schemeWithAppId.isMiniAppUrl("mscheme.$TEST_ID_MINIAPP://") shouldEqualTo true
        schemeWithAppId.isMiniAppUrl("https://mscheme.$TEST_ID_MINIAPP/") shouldEqualTo true
    }

    @Test
    fun `isMiniAppUrl with custom url should return the expected values`() {
        schemeWithCustomUrl.appUrl shouldEqual TEST_URL_HTTPS_1
        schemeWithCustomUrl.isMiniAppUrl(TEST_URL_HTTPS_1) shouldEqualTo true
    }
    /** end region */

    /** region: appendParametersToUrl */
    @Test
    fun `appendParametersToUrl should return the url when query is empty`() {
        val expectedUrl = "mscheme.$TEST_ID_MINIAPP://"
        schemeWithAppId.appendParametersToUrl(expectedUrl, "") shouldEqual expectedUrl
    }

    @Test
    fun `appendParametersToUrl should return the appended url when query is not empty`() {
        val expectedUrl = "mscheme.$TEST_ID_MINIAPP://"
        val expectedQuery = TEST_URL_PARAMS
        schemeWithAppId.appendParametersToUrl(
            expectedUrl,
            expectedQuery
        ) shouldEqual "$expectedUrl?$expectedQuery"
    }

    /** end region */

    @Test
    fun `resolveParameters should append qus mark prior to the parameters`() {
        val query = "param1=value1&param2=value2"
        schemeWithAppId.resolveParameters(query) shouldEqual "?$query"
    }
}
