package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
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
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MiniAppWebviewTest {
    private lateinit var context: Context
    private lateinit var basePath: String
    private lateinit var miniAppWebView: MiniAppWebView
    private lateinit var webResourceRequest: WebResourceRequest
    private val miniAppMessageBridge: MiniAppMessageBridge = mock()

    @Before
    fun setup() {
        context = getApplicationContext()
        basePath = context.filesDir.path
        miniAppWebView = MiniAppWebView(
            context,
            basePath = basePath,
            appId = TEST_MA_ID,
            miniAppMessageBridge = miniAppMessageBridge
        )
        webResourceRequest = getWebResReq(miniAppWebView.getLoadUrl().toUri())
    }

    @Test
    fun `for a given app id, creates corresponding view for the caller`() {
        miniAppWebView.url shouldContain miniAppWebView.appId
    }

    @Test
    fun `should have corrected load url format`() {
        miniAppWebView.getLoadUrl() shouldEqual "https://mscheme.${miniAppWebView.appId}/miniapp/index.html"
    }

    @Test
    fun `when MiniAppWebView is created then LayoutParams use MATCH_PARENT for dimensions`() {
        miniAppWebView.layoutParams.width shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
        miniAppWebView.layoutParams.height shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
    }

    @Test
    fun `when MiniAppWebView is created then javascript should be enabled`() {
        assertTrue { miniAppWebView.settings.javaScriptEnabled }
    }

    @Test
    fun `when MiniAppWebView is created then allowUniversalAccessFromFileURLs should be enabled`() {
        assertTrue { miniAppWebView.settings.allowUniversalAccessFromFileURLs }
    }

    @Test
    fun `when MiniAppWebView is created then domStorageEnabled should be enabled`() {
        miniAppWebView.settings.allowUniversalAccessFromFileURLs shouldBe true
    }

    @Test
    fun `when MiniAppWebView is created then databaseEnabled should be enabled`() {
        miniAppWebView.settings.allowUniversalAccessFromFileURLs shouldBe true
    }

    @Test
    fun `when destroyView called then the MiniAppWebView should be disposed`() {
        val displayer = Mockito.spy(miniAppWebView)
        displayer.destroyView()

        verify(displayer, times(1)).stopLoading()
        displayer.webViewClient shouldBe null
        verify(displayer, times(1)).destroy()
    }

    @Test
    fun `for a WebViewClient, it should be MiniAppWebViewClient`() {
        miniAppWebView.webViewClient shouldBeInstanceOf MiniAppWebViewClient::class
    }

    @Test
    fun `each mini app should have different domain`() {
        val miniAppWebViewForMiniapp1 = MiniAppWebView(
            context, miniAppWebView.basePath, "app-id-1", miniAppMessageBridge)
        val miniAppWebViewForMiniapp2 = MiniAppWebView(
            context, miniAppWebView.basePath, "app-id-2", miniAppMessageBridge)
        miniAppWebViewForMiniapp1.url shouldNotBeEqualTo miniAppWebViewForMiniapp2.url
    }

    @Test
    fun `MiniAppMessageBridge should be connected with RealMiniAppDisplay`() {
        verify(miniAppMessageBridge, atLeastOnce()).setWebViewListener(miniAppWebView)
    }

    @Test
    fun `should intercept request with WebViewAssetLoader`() {
        val webAssetLoader: WebViewAssetLoader =
            Mockito.spy((miniAppWebView.webViewClient as MiniAppWebViewClient).loader)
        val webViewClient = MiniAppWebViewClient(context, webAssetLoader,
            "custom_domain", "custom_scheme")
        val webResourceRequest = getWebResReq(TEST_URL_HTTPS_1.toUri())

        webViewClient.shouldInterceptRequest(miniAppWebView, webResourceRequest)

        verify(webAssetLoader, times(1))
            .shouldInterceptRequest(webResourceRequest.url)
    }

    @Test
    fun `should redirect to custom domain when only loading with custom scheme`() {
        val webAssetLoader: WebViewAssetLoader = (miniAppWebView.webViewClient as MiniAppWebViewClient).loader
        val customDomain = "https://mscheme.${miniAppWebView.appId}/"
        val webViewClient = MiniAppWebViewClient(context, webAssetLoader,
            customDomain, "mscheme.${miniAppWebView.appId}://")

        val displayer = Mockito.spy(miniAppWebView)

        webViewClient.onReceivedError(displayer, webResourceRequest, mock())
        webViewClient.onReceivedError(displayer,
            getWebResReq("mscheme.${miniAppWebView.appId}://".toUri()), mock())

        verify(displayer, times(1)).loadUrl(customDomain)
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
