package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.webkit.WebViewAssetLoader
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_URL_HTTPS_1
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RealMiniAppDisplayTest {
    private lateinit var context: Context
    private lateinit var basePath: String
    private lateinit var realDisplay: RealMiniAppDisplay
    private lateinit var webResourceRequest: WebResourceRequest
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
        webResourceRequest = getWebResReq(realDisplay.getLoadUrl().toUri())
    }

    @Test
    fun `for a given app id, RealMiniAppDisplay creates corresponding view for the caller`() =
        runBlockingTest {
            realDisplay.url shouldContain realDisplay.appId
        }

    @Test
    fun `for a given basePath, getMiniAppView should not return WebView to the caller`() =
        runBlockingTest {
            realDisplay.getMiniAppView() shouldNotHaveTheSameClassAs WebView::class
        }

    @Test
    fun `should have corrected load url format`() {
        realDisplay.getLoadUrl() shouldEqual "https://mscheme.${realDisplay.appId}/miniapp/index.html"
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
    fun `when destroyView called then the realDisplay should be disposed`() {
        val displayer: RealMiniAppDisplay = Mockito.spy(realDisplay)
        displayer.destroyView()

        verify(displayer, times(1)).stopLoading()
        displayer.webViewClient shouldBe null
        verify(displayer, times(1)).destroy()
    }

    @Test
    fun `for a WebViewClient, it should be MiniAppWebViewClient`() {
        realDisplay.webViewClient shouldBeInstanceOf MiniAppWebViewClient::class
    }

    @Test
    fun `each mini app should have different domain`() {
        val realDisplayForMiniapp1 = RealMiniAppDisplay(context, realDisplay.basePath, "app-id-1", miniAppMessageBridge)
        val realDisplayForMiniapp2 = RealMiniAppDisplay(context, realDisplay.basePath, "app-id-2", miniAppMessageBridge)
        realDisplayForMiniapp1.url shouldNotBeEqualTo realDisplayForMiniapp2.url
    }

    @Test
    fun `MiniAppMessageBridge should be connected with RealMiniAppDisplay`() {
        verify(miniAppMessageBridge, atLeastOnce()).setWebViewListener(realDisplay)
    }

    @Test
    fun `should intercept request with WebViewAssetLoader`() {
        val webAssetLoader: WebViewAssetLoader = Mockito.spy((realDisplay.webViewClient as MiniAppWebViewClient).loader)
        val webViewClient = MiniAppWebViewClient(context, webAssetLoader,
            "custom_domain", "custom_scheme")
        val webResourceRequest = getWebResReq(TEST_URL_HTTPS_1.toUri())

        webViewClient.shouldInterceptRequest(realDisplay, webResourceRequest)

        verify(webAssetLoader, times(1))
            .shouldInterceptRequest(webResourceRequest.url)
    }

    @Test
    fun `should redirect to custom domain when only loading with custom scheme`() {
        val webAssetLoader: WebViewAssetLoader = (realDisplay.webViewClient as MiniAppWebViewClient).loader
        val customDomain = "https://mscheme.${realDisplay.appId}/"
        val webViewClient = MiniAppWebViewClient(context, webAssetLoader,
            customDomain, "mscheme.${realDisplay.appId}://")

        val displayer = Mockito.spy(realDisplay)

        webViewClient.onReceivedError(displayer, webResourceRequest, mock())
        webViewClient.onReceivedError(displayer,
            getWebResReq("mscheme.${realDisplay.appId}://".toUri()), mock())

        verify(displayer, times(1)).loadUrl("$customDomain")
    }

    private fun getWebResReq(uriReq: Uri): WebResourceRequest {
        return object : WebResourceRequest {
            override fun getUrl(): Uri = uriReq

            override fun isRedirect(): Boolean = false

            override fun getMethod(): String = "GET"

            override fun getRequestHeaders(): MutableMap<String, String> = HashMap()

            override fun hasGesture(): Boolean = false

            override fun isForMainFrame(): Boolean = false
        }
    }
}
