package com.rakuten.tech.mobile.miniapp.display

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AndroidRuntimeException
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.webkit.WebViewAssetLoader
import com.nhaarman.mockitokotlin2.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppExternalUrlLoader
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.io.ByteArrayInputStream
import kotlin.test.assertTrue

open class BaseWebViewSpec {
    lateinit var context: Context
    lateinit var basePath: String
    internal lateinit var miniAppWebView: MiniAppWebView
    lateinit var webResourceRequest: WebResourceRequest
    val miniAppMessageBridge: MiniAppMessageBridge = mock()
    val miniAppNavigator: MiniAppNavigator = mock()
    internal val miniAppCustomPermissionCache: MiniAppCustomPermissionCache = mock()
    internal lateinit var webChromeClient: MiniAppWebChromeClient
    lateinit var activityScenario: ActivityScenario<TestActivity>

    @Suppress("LongMethod")
    @Before
    open fun setup() {
        activityScenario = ActivityScenario.launch(TestActivity::class.java)
        activityScenario.onActivity { activity ->
            context = activity
            basePath = context.filesDir.path
            webChromeClient = Mockito.spy(MiniAppWebChromeClient(context, TEST_MA, miniAppCustomPermissionCache))

            miniAppWebView = MiniAppWebView(
                context,
                basePath = basePath,
                miniAppInfo = TEST_MA,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = miniAppNavigator,
                hostAppUserAgentInfo = TEST_HA_NAME,
                miniAppWebChromeClient = webChromeClient,
                miniAppCustomPermissionCache = miniAppCustomPermissionCache
            )
            webResourceRequest = getWebResReq(miniAppWebView.getLoadUrl().toUri())
        }
    }
}

@RunWith(AndroidJUnit4::class)
class MiniAppHTTPWebViewSpec : BaseWebViewSpec() {

    override fun setup() {
        super.setup()
        miniAppWebView = MiniAppHttpWebView(
                context,
                miniAppInfo = TEST_MA,
                appUrl = TEST_MA_URL,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = miniAppNavigator,
                hostAppUserAgentInfo = TEST_HA_NAME,
                miniAppWebChromeClient = webChromeClient,
                miniAppCustomPermissionCache = miniAppCustomPermissionCache
        )
    }

    @Test
    fun `should remove cached permission data when window is closed`() {
        (miniAppWebView as MiniAppHttpWebView).callOnDetached()
        verify(miniAppCustomPermissionCache, times(1)).removePermission(anyString())
    }
}

@RunWith(AndroidJUnit4::class)
class MiniAppWebviewSpec : BaseWebViewSpec() {

    @Test
    fun `for a given app id, creates corresponding view for the caller`() {
        miniAppWebView.url shouldContain miniAppWebView.miniAppInfo.id
    }

    @Test
    fun `should have corrected load url format`() {
        miniAppWebView.getLoadUrl() shouldEqual "https://mscheme.${miniAppWebView.miniAppInfo.id}/miniapp/index.html"
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
        miniAppWebView.settings.domStorageEnabled shouldBe true
    }

    @Test
    fun `when MiniAppWebView is created then databaseEnabled should be enabled`() {
        miniAppWebView.settings.databaseEnabled shouldBe true
    }

    @Test
    fun `when MiniAppWebView is created then mediaPlaybackRequiresUserGesture should be disabled`() {
        miniAppWebView.settings.mediaPlaybackRequiresUserGesture shouldBe false
    }

    @Test
    fun `when MiniAppWebView is created then user-agent contains host app info`() {
        miniAppWebView.hostAppUserAgentInfo shouldBe TEST_HA_NAME
        miniAppWebView.settings.userAgentString shouldEndWith TEST_HA_NAME
    }

    @Test
    fun `should keep user-agent unchanged when host app info is empty`() {
        miniAppWebView = MiniAppWebView(
            context,
            basePath = basePath,
            miniAppInfo = TEST_MA,
            miniAppMessageBridge = miniAppMessageBridge,
            miniAppNavigator = miniAppNavigator,
            hostAppUserAgentInfo = "",
            miniAppWebChromeClient = webChromeClient,
            miniAppCustomPermissionCache = mock()
        )
        miniAppWebView.settings.userAgentString shouldNotEndWith TEST_HA_NAME
    }

    @Test
    fun `when destroyView called then the MiniAppWebView should be disposed`() {
        val displayer = Mockito.spy(miniAppWebView)
        displayer.destroyView()

        verify(displayer).stopLoading()
        displayer.webViewClient shouldBe null

        verify(displayer).destroy()
    }

    @Test
    fun `should not restore the original state of activity when activity is destroyed`() {
        val displayer = Mockito.spy(miniAppWebView)
        (context as Activity).setContentView(displayer)
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
        (context as Activity).setContentView(R.layout.browser_actions_context_menu_page)

        verify(displayer.miniAppWebChromeClient, times(0)).onWebViewDetach()
        verify(miniAppMessageBridge, times(0)).onWebViewDetach()
    }

    @Test
    fun `should trigger executions when attach and detach webview`() {
        val displayer = Mockito.spy(miniAppWebView)
        (context as Activity).setContentView(displayer)
        (context as Activity).setContentView(R.layout.browser_actions_context_menu_page)

        verify(displayer, times(1)).onResume()
        verify(displayer).onPause()
        verify(displayer.miniAppWebChromeClient).onWebViewDetach()
        verify(miniAppMessageBridge).onWebViewDetach()
    }

    @Test
    fun `each mini app should have different domain`() {
        val miniAppWebViewForMiniapp1 = MiniAppWebView(
            context,
            miniAppWebView.basePath,
            TEST_MA,
            miniAppMessageBridge,
            miniAppNavigator,
            TEST_HA_NAME,
            mock(),
            mock()
        )
        val miniAppWebViewForMiniapp2 = MiniAppWebView(
            context, miniAppWebView.basePath, TEST_MA.copy(id = "app-id-2"), miniAppMessageBridge,
            miniAppNavigator, TEST_HA_NAME, mock(), mock())
        miniAppWebViewForMiniapp1.url shouldNotBeEqualTo miniAppWebViewForMiniapp2.url
    }

    @Test
    fun `should load url which is internal mini app scheme from external emission`() {
        miniAppWebView.externalResultHandler.emitResult(TEST_URL_HTTPS_1)
        miniAppWebView.externalResultHandler.emitResult("mscheme.${miniAppWebView.miniAppInfo.id}://index.html")
        val intent = Intent().apply {
            putExtra(MiniAppExternalUrlLoader.returnUrlTag,
                "https://mscheme.${miniAppWebView.miniAppInfo.id}/miniapp/index.html")
        }
        miniAppWebView.externalResultHandler.emitResult(intent)

        miniAppWebView.getLoadUrl() shouldBeEqualTo
                "https://mscheme.${miniAppWebView.miniAppInfo.id}/miniapp/index.html"
    }

    @Test
    fun `should send response for the specified ID over the mini app bridge`() {
        val spyMiniAppWebView = spy(miniAppWebView)
        spyMiniAppWebView.runSuccessCallback("test_id", "test_value")

        Verify on spyMiniAppWebView that spyMiniAppWebView.evaluateJavascript(
            argWhere { it.contains("""MiniAppBridge.execSuccessCallback(`test_id`""") }, any())
    }

    @Test
    fun `should send response with escaped backtick characters`() {
        val spyMiniAppWebView = spy(miniAppWebView)
        spyMiniAppWebView.runSuccessCallback("test_id", "`test response`")

        Verify on spyMiniAppWebView that spyMiniAppWebView.evaluateJavascript(
            argWhere { it.contains("""`\`test response\``""") }, any())
    }
}

@Suppress("SwallowedException", "LongMethod")
@RunWith(AndroidJUnit4::class)
class MiniAppWebClientSpec : BaseWebViewSpec() {
    private val externalResultHandler: ExternalResultHandler = spy()
    private val miniAppScheme = Mockito.spy(MiniAppScheme.schemeWithAppId(TEST_MA_ID))

    @Test
    fun `for a WebViewClient, it should be MiniAppWebViewClient`() {
        miniAppWebView.webViewClient shouldBeInstanceOf MiniAppWebViewClient::class
    }

    @Test
    fun `should intercept request with WebViewAssetLoader`() {
        val webAssetLoader: WebViewAssetLoader? =
            Mockito.spy((miniAppWebView.webViewClient as MiniAppWebViewClient).loader)
        val webViewClient = MiniAppWebViewClient(context, webAssetLoader, miniAppNavigator,
            externalResultHandler, miniAppScheme)
        val webResourceRequest = getWebResReq(TEST_URL_HTTPS_1.toUri())

        webViewClient.shouldInterceptRequest(miniAppWebView, webResourceRequest)

        verify(webAssetLoader!!, times(1))
            .shouldInterceptRequest(webResourceRequest.url)
    }

    @Test
    fun `should redirect to custom domain when only loading with custom scheme`() {
        val webAssetLoader: WebViewAssetLoader? = (miniAppWebView.webViewClient as MiniAppWebViewClient).loader
        val customDomain = "https://mscheme.${miniAppWebView.miniAppInfo.id}/"
        val webViewClient = Mockito.spy(MiniAppWebViewClient(context, webAssetLoader, miniAppNavigator,
            externalResultHandler, miniAppScheme))

        val displayer = Mockito.spy(miniAppWebView)

        webViewClient.onReceivedError(displayer, webResourceRequest, mock())
        webViewClient.onReceivedError(displayer,
            getWebResReq("mscheme.${miniAppWebView.miniAppInfo.id}://".toUri()), mock())

        verify(webViewClient, times(1)).loadWithCustomDomain(displayer, customDomain)
    }

    @Test
    fun `should open phone dialer when there is telephone scheme`() {
        val webAssetLoader: WebViewAssetLoader? = (miniAppWebView.webViewClient as MiniAppWebViewClient).loader
        val webViewClient = Mockito.spy(MiniAppWebViewClient(context, webAssetLoader, miniAppNavigator,
            externalResultHandler, miniAppScheme))
        val displayer = Mockito.spy(miniAppWebView)

        try {
            webViewClient.shouldOverrideUrlLoading(view = displayer, url = null)
            webViewClient.shouldOverrideUrlLoading(displayer, miniAppWebView.getLoadUrl())
            webViewClient.shouldOverrideUrlLoading(displayer, TEST_PHONE_URI)
        } catch (e: AndroidRuntimeException) {
            // context here is not activity
        }

        verify(miniAppScheme, times(1)).openPhoneDialer(context, TEST_PHONE_URI)
    }

    @Test
    fun `should have default external link handler when there is no config for navigation`() {
        val displayer = Mockito.spy(MiniAppWebView(
            context,
            basePath = basePath,
            miniAppInfo = TEST_MA,
            miniAppMessageBridge = miniAppMessageBridge,
            miniAppNavigator = null,
            hostAppUserAgentInfo = TEST_HA_NAME,
            miniAppWebChromeClient = webChromeClient,
            miniAppCustomPermissionCache = miniAppCustomPermissionCache
        ))
        val miniAppNavigator = Mockito.spy(displayer.miniAppNavigator)
        val webAssetLoader: WebViewAssetLoader? = (displayer.webViewClient as MiniAppWebViewClient).loader
        val webViewClient = Mockito.spy(MiniAppWebViewClient(context, webAssetLoader, miniAppNavigator!!,
            externalResultHandler, miniAppScheme))

        try {
            webViewClient.shouldOverrideUrlLoading(displayer, TEST_URL_HTTPS_1)
        } catch (e: AndroidRuntimeException) {
            // context here is not activity
        }

        miniAppNavigator shouldNotBe null
        verify(miniAppNavigator).openExternalUrl(TEST_URL_HTTPS_1, externalResultHandler)
    }

    @Test
    fun `should open external url loader when there is the config for navigation`() {
        val webAssetLoader: WebViewAssetLoader? = (miniAppWebView.webViewClient as MiniAppWebViewClient).loader
        val webViewClient = Mockito.spy(MiniAppWebViewClient(context, webAssetLoader, miniAppNavigator,
            externalResultHandler, miniAppScheme))
        val displayer = Mockito.spy(miniAppWebView)

        try {
            webViewClient.shouldOverrideUrlLoading(displayer, TEST_URL_HTTPS_1)
        } catch (e: AndroidRuntimeException) {
            // context here is not activity
        }

        verify(miniAppNavigator).openExternalUrl(TEST_URL_HTTPS_1, externalResultHandler)
    }

    @Test
    fun `should not intercept mime type for regular cases`() {
        val webResourceResponse = WebResourceResponse("", "utf-8", ByteArrayInputStream("".toByteArray()))
        val webClient = miniAppWebView.webViewClient as MiniAppWebViewClient
        val request = getWebResReq("mscheme.${miniAppWebView.miniAppInfo.id}://".toUri())
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
class MiniAppWebChromeTest : BaseWebViewSpec() {

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
        val webClient = MiniAppWebChromeClient(mock(), TEST_MA, mock())
        webClient.bridgeJs shouldBe null
    }

    @Test
    fun `should allow geolocation callback when custom permission is allowed`() {
        doReturn(true).whenever(miniAppCustomPermissionCache)
            .hasPermission(TEST_MA_ID, MiniAppCustomPermissionType.LOCATION)

        val geoLocationCallback = Mockito.spy(
            GeolocationPermissions.Callback { _, allow, retain ->
                allow shouldBe true
                retain shouldBe false
            }
        )
        webChromeClient.onGeolocationPermissionsShowPrompt("", geoLocationCallback)

        verify(geoLocationCallback, times(1)).invoke("", true, false)
    }

    @Test
    fun `should not allow geolocation callback when custom permission is denied`() {
        doReturn(false).whenever(miniAppCustomPermissionCache)
            .hasPermission(TEST_MA_ID, MiniAppCustomPermissionType.LOCATION)

        val geoLocationCallback = Mockito.spy(
            GeolocationPermissions.Callback { _, allow, retain ->
                allow shouldBe false
                retain shouldBe false
            }
        )
        webChromeClient.onGeolocationPermissionsShowPrompt("", geoLocationCallback)

        verify(geoLocationCallback, times(1)).invoke("", false, false)
    }

    @Test
    fun `should override js dialog event`() {
        webChromeClient.onJsAlert(
            miniAppWebView, TEST_URL_HTTPS_2, TEST_BODY_CONTENT, mock()) shouldBe true
        webChromeClient.onJsConfirm(
            miniAppWebView, TEST_URL_HTTPS_2, TEST_BODY_CONTENT, mock()) shouldBe true
        webChromeClient.onJsPrompt(
            miniAppWebView, TEST_URL_HTTPS_2, TEST_BODY_CONTENT, TEST_VALUE, mock()) shouldBe true
        webChromeClient.onJsPrompt(
            miniAppWebView, TEST_URL_HTTPS_2, TEST_BODY_CONTENT, null, mock()) shouldBe true
    }

    @Test
    fun `should close custom view when exit`() {
        val context = getApplicationContext<Context>()
        webChromeClient = Mockito.spy(MiniAppWebChromeClient(context, TEST_MA, mock()))
        webChromeClient.onShowCustomView(null, mock())
        webChromeClient.customView = mock()
        webChromeClient.onShowCustomView(mock(), mock())

        verify(webChromeClient).onHideCustomView()
    }

    @Test
    fun `should execute custom view flow without error`() {
        val webChromeClient = Mockito.spy(MiniAppWebChromeClient(context, TEST_MA, mock()))

        webChromeClient.onShowCustomView(View(context), mock())
        webChromeClient.updateControls()
        webChromeClient.onHideCustomView()
        webChromeClient.updateControls()
    }

    @Test
    fun `should exit fullscreen when destroy miniapp view`() {
        val webChromeClient = Mockito.spy(MiniAppWebChromeClient(context, TEST_MA, mock()))
        webChromeClient.onShowCustomView(View(context), mock())
        webChromeClient.onWebViewDetach()

        verify(webChromeClient).onHideCustomView()
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
