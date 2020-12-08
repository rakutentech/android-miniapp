package com.rakuten.tech.mobile.miniapp.navigator

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_PHONE_URI
import com.rakuten.tech.mobile.miniapp.TEST_URL_HTTPS_1
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiniAppExternalUrlLoaderSpec {
    private val miniAppSchemeId = MiniAppScheme.schemeWithAppId(TEST_MA_ID)
    private lateinit var externalUrlLoader: MiniAppExternalUrlLoader
    private lateinit var externalUrlLoaderUrlBased: MiniAppExternalUrlLoader

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            externalUrlLoader = MiniAppExternalUrlLoader.loaderWithId(TEST_MA_ID, activity)
            externalUrlLoaderUrlBased = MiniAppExternalUrlLoader.loaderWithUrl(TEST_MA_URL, activity)
        }
    }

    @Test
    fun `should only override url when it is supported by mini app view`() {
        externalUrlLoader.shouldOverrideUrlLoading(TEST_URL_HTTPS_1) shouldBe false
        externalUrlLoader.shouldOverrideUrlLoading(miniAppSchemeId.miniAppCustomScheme) shouldBe true
        externalUrlLoader.shouldOverrideUrlLoading(miniAppSchemeId.miniAppCustomDomain) shouldBe true
        externalUrlLoader.shouldOverrideUrlLoading(TEST_PHONE_URI) shouldBe true
    }

    @Test
    fun `should not override phone url when there is no activity context`() {
        val externalUrlLoader = MiniAppExternalUrlLoader.loaderWithId(TEST_MA_ID, null)
        externalUrlLoader.shouldOverrideUrlLoading(TEST_PHONE_URI) shouldBe false
    }

    @Test
    fun `should only override url when it is supported by mini app view (url based)`() {
        externalUrlLoaderUrlBased.shouldOverrideUrlLoading(TEST_URL_HTTPS_1) shouldBe false
        externalUrlLoaderUrlBased.shouldOverrideUrlLoading(TEST_MA_URL) shouldBe true
        externalUrlLoaderUrlBased.shouldOverrideUrlLoading(TEST_PHONE_URI) shouldBe true
    }

    @Test
    fun `should not override phone url when there is no activity context (url based)`() {
        val externalUrlLoader = MiniAppExternalUrlLoader.loaderWithUrl(TEST_MA_URL, null)
        externalUrlLoader.shouldOverrideUrlLoading(TEST_PHONE_URI) shouldBe false
    }
}
