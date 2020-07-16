package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.webkit.WebViewAssetLoader
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_HA_NAME
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_URL_HTTPS_1
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.io.ByteArrayInputStream
import kotlin.test.assertTrue

open class BaseWebViewTest {
    lateinit var context: Context
    lateinit var basePath: String
    internal lateinit var miniAppWebView: MiniAppWebView
    lateinit var webResourceRequest: WebResourceRequest
    val miniAppMessageBridge: MiniAppMessageBridge = mock()
    internal lateinit var webChromeClient: MiniAppWebChromeClient

    @Before
    fun setup() {
        context = getApplicationContext()
        basePath = context.filesDir.path
        webChromeClient = Mockito.spy(MiniAppWebChromeClient(context))

        miniAppWebView = MiniAppWebView(
            context,
            basePath = basePath,
            appId = TEST_MA_ID,
            miniAppMessageBridge = miniAppMessageBridge,
            hostAppInfo = TEST_HA_NAME,
            miniAppWebChromeClient = webChromeClient
        )
        webResourceRequest = getWebResReq(miniAppWebView.getLoadUrl().toUri())
    }
}

@RunWith(AndroidJUnit4::class)
class MiniAppWebviewTest : BaseWebViewTest() {

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
    fun `when MiniAppWebView is created then user-agent contains host app info`() {
        miniAppWebView.hostAppInfo shouldBe TEST_HA_NAME
        miniAppWebView.settings.userAgentString shouldEndWith TEST_HA_NAME
    }

    @Test
    fun `should keep user-agent unchanged when host app info is empty`() {
        miniAppWebView = MiniAppWebView(
            context,
            basePath = basePath,
            appId = TEST_MA_ID,
            miniAppMessageBridge = miniAppMessageBridge,
            hostAppInfo = "",
            miniAppWebChromeClient = webChromeClient
        )
        miniAppWebView.settings.userAgentString shouldNotEndWith TEST_HA_NAME
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
    fun `each mini app should have different domain`() {
        val miniAppWebViewForMiniapp1 = MiniAppWebView(
            context, miniAppWebView.basePath, "app-id-1", miniAppMessageBridge, TEST_HA_NAME)
        val miniAppWebViewForMiniapp2 = MiniAppWebView(
            context, miniAppWebView.basePath, "app-id-2", miniAppMessageBridge, TEST_HA_NAME)
        miniAppWebViewForMiniapp1.url shouldNotBeEqualTo miniAppWebViewForMiniapp2.url
    }

    @Test
    fun `MiniAppMessageBridge should be connected with RealMiniAppDisplay`() {
        verify(miniAppMessageBridge, atLeastOnce()).setWebViewListener(miniAppWebView)
    }
}

@RunWith(AndroidJUnit4::class)
class MiniAppWebClientTest : BaseWebViewTest() {

    @Test
    fun `for a WebViewClient, it should be MiniAppWebViewClient`() {
        miniAppWebView.webViewClient shouldBeInstanceOf MiniAppWebViewClient::class
    }

    @Test
    fun `should intercept request with WebViewAssetLoader`() {
        val webAssetLoader: WebViewAssetLoader =
            Mockito.spy((miniAppWebView.webViewClient as MiniAppWebViewClient).loader)
        val webViewClient = MiniAppWebViewClient(webAssetLoader,
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
        val webViewClient = Mockito.spy(MiniAppWebViewClient(webAssetLoader, customDomain,
            "mscheme.${miniAppWebView.appId}://"))

        val displayer = Mockito.spy(miniAppWebView)

        webViewClient.onReceivedError(displayer, webResourceRequest, mock())
        webViewClient.onReceivedError(displayer,
            getWebResReq("mscheme.${miniAppWebView.appId}://".toUri()), mock())

        verify(webViewClient, times(1)).loadWithCustomDomain(displayer, customDomain)
    }

    @Test
    fun `should not intercept mime type for regular cases`() {
        val webResourceResponse = WebResourceResponse("", "utf-8", ByteArrayInputStream("".toByteArray()))
        val webClient = miniAppWebView.webViewClient as MiniAppWebViewClient
        val request = getWebResReq("mscheme.${miniAppWebView.appId}://".toUri())
        val secondRequest = Mockito.spy(getWebResReq("test.js".toUri()))

        webClient.interceptMimeType(webResourceResponse, request)
        webResourceResponse.mimeType shouldBe ""

        When calling secondRequest.url itReturns null
        webClient.interceptMimeType(webResourceResponse, secondRequest)
        webResourceResponse.mimeType shouldBe ""
    }

    @Test
    fun `should define correct mime type for js`() {
        val webResourceResponse = WebResourceResponse("", "utf-8", ByteArrayInputStream("".toByteArray()))
        val webClient = miniAppWebView.webViewClient as MiniAppWebViewClient

        val request = getWebResReq("test.js".toUri())
        webClient.interceptMimeType(webResourceResponse, request)

        webResourceResponse.mimeType shouldBe "application/javascript"
    }
}

@RunWith(AndroidJUnit4::class)
class MiniAppWebChromeTest : BaseWebViewTest() {

    @Test
    fun `for a WebChromeClient, it should be MiniAppWebChromeClient`() {
        miniAppWebView.webChromeClient shouldBeInstanceOf MiniAppWebChromeClient::class
    }

    @Test
    fun `should do js injection when there is a change in the document title`() {
        val webChromeClient = Mockito.spy(miniAppWebView.webChromeClient as MiniAppWebChromeClient)
        webChromeClient.onReceivedTitle(miniAppWebView, "web_title")

        verify(webChromeClient, times(1)).doInjection(miniAppWebView)
    }

    @Test
    fun `bridge js should be null when js asset is inaccessible`() {
        val webClient = MiniAppWebChromeClient(mock())
        webClient.bridgeJs shouldBe null
    }

    @Test
    fun `should invoke callback from onRequestPermissionsResult when it is called`() {
        val geoLocationCallback = Mockito.spy(
            GeolocationPermissions.Callback { origin, allow, retain ->
                allow shouldBe true
                retain shouldBe false
            }
        )

        webChromeClient.onGeolocationPermissionsShowPrompt("", geoLocationCallback)
        webChromeClient.onGeolocationPermissionsShowPrompt(null, null)

        verify(geoLocationCallback, times(1)).invoke("", true, false)
    }
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
