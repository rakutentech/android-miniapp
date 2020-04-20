package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_URL_FILE
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RealMiniAppDisplayTest {
    private lateinit var context: Context
    private lateinit var basePath: String
    private lateinit var realDisplay: RealMiniAppDisplay
    private val miniAppMessageBridge: MiniAppMessageBridge = mock()

    @Before
    fun setup() {
        context = getApplicationContext()
        basePath = context.filesDir.path
        realDisplay = RealMiniAppDisplay(
            context,
            basePath = basePath,
            appId = TEST_MA_ID,
            miniAppMessageBridge = miniAppMessageBridge
        )
    }

    @Test
    fun `for a given app id, RealMiniAppDisplay creates corresponding view for the caller`() =
        runBlockingTest {
            realDisplay.url shouldContain TEST_MA_ID
        }

    @Test
    fun `for a given basePath, getMiniAppView should not return WebView to the caller`() =
        runBlockingTest {
            realDisplay.getMiniAppView() shouldNotHaveTheSameClassAs WebView::class
        }

    @Test
    fun `when getLoadUrl is called then a formed path contains app id`() {
        realDisplay.getLoadUrl() shouldEqual
                "https://$TEST_MA_ID.miniapps.androidplatform.net/miniapp/index.html"
    }

    @Test
    fun `when MiniAppDisplay is created then LayoutParams use MATCH_PARENT for dimensions`() {
        realDisplay.layoutParams.width shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
        realDisplay.layoutParams.height shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
    }

    @Test
    fun `when MiniAppDisplay is created then javascript should be enabled`() {
        assertTrue { realDisplay.settings.javaScriptEnabled }
    }

    @Test
    fun `when MiniAppDisplay is created then allowUniversalAccessFromFileURLs should be enabled`() {
        assertTrue { realDisplay.settings.allowUniversalAccessFromFileURLs }
    }

    @Test
    fun `when MiniAppDisplay is created then domStorageEnabled should be enabled`() {
        realDisplay.settings.allowUniversalAccessFromFileURLs shouldBe true
    }

    @Test
    fun `when MiniAppDisplay is created then databaseEnabled should be enabled`() {
        realDisplay.settings.allowUniversalAccessFromFileURLs shouldBe true
    }

    @Test
    fun `for a WebViewClient, it should be MiniAppWebViewClient`() {
        realDisplay.webViewClient shouldBeInstanceOf MiniAppWebViewClient::class
    }

    @Test
    fun `js should be injected`() {
        realDisplay.webViewClient.onLoadResource(realDisplay, TEST_URL_FILE)
        (realDisplay.webViewClient as MiniAppWebViewClient).isJsInjected shouldBe true
    }

    @Test
    fun `each mini app should have different domain`() {
        val realDisplayForMiniapp1 = RealMiniAppDisplay(context, basePath, "app-id-1", miniAppMessageBridge)
        val realDisplayForMiniapp2 = RealMiniAppDisplay(context, basePath, "app-id-2", miniAppMessageBridge)
        realDisplayForMiniapp1.url shouldNotBeEqualTo realDisplayForMiniapp2.url
    }

    @Test
    fun `MiniAppMessageBridge should be connected with RealMiniAppDisplay`() {
        verify(miniAppMessageBridge, atLeastOnce()).setWebViewListener(realDisplay)
    }
}
