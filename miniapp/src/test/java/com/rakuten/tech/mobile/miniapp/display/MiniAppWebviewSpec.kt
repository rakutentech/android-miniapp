package com.rakuten.tech.mobile.miniapp.display

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.webkit.WebViewAssetLoader
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppExternalUrlLoader
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalAnswers.delegatesTo
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException
import org.mockito.kotlin.*
import org.mockito.kotlin.mock
import java.io.ByteArrayInputStream
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
open class BaseWebViewSpec {
    lateinit var context: Context
    lateinit var basePath: String
    internal lateinit var miniAppWebView: MiniAppWebView
    lateinit var webResourceRequest: WebResourceRequest
    val miniAppMessageBridge: MiniAppMessageBridge = mock()
    val miniAppNavigator: MiniAppNavigator = mock()
    val miniAppFileChooser: MiniAppFileChooser = mock()
    internal val miniAppCustomPermissionCache: MiniAppCustomPermissionCache = mock()
    internal lateinit var webChromeClient: MiniAppWebChromeClient
    var activityScenario = ActivityScenario.launch(TestActivity::class.java)

    @Before
    open fun setup() {
        activityScenario.onActivity { activity ->
            context = activity
            basePath = context.filesDir.path
            webChromeClient = Mockito.spy(
                MiniAppWebChromeClient(
                    context,
                    TEST_MA,
                    miniAppCustomPermissionCache,
                    miniAppFileChooser,
                    mock()
                )
            )
            miniAppWebView = createMiniAppWebView()
            webResourceRequest = getWebResReq(miniAppWebView.getLoadUrl().toUri())
        }
    }

    @After
    fun finish() {
        activityScenario.close()
    }

    private fun createMiniAppWebView() = MiniAppWebView(
        context,
        basePath = basePath,
        miniAppInfo = TEST_MA,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        miniAppFileChooser = miniAppFileChooser,
        hostAppUserAgentInfo = TEST_HA_NAME,
        miniAppWebChromeClient = webChromeClient,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache,
        downloadedManifestCache = mock(),
        queryParams = TEST_URL_PARAMS,
        ratDispatcher = mock(),
        secureStorageDispatcher = mock(),
        enableH5Ads = false,
        miniAppIAPVerifier = mock()
    )

    @Test
    fun `runSuccessfullCallback should call post`() {
        miniAppWebView = spy(createMiniAppWebView())
        miniAppWebView.runSuccessCallback(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
        verify(miniAppWebView).post(Mockito.any())
    }

    @Test
    fun `runErrorCallback should call post`() {
        miniAppWebView = spy(createMiniAppWebView())
        miniAppWebView.runErrorCallback(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
        verify(miniAppWebView).post(Mockito.any())
    }

    @Test
    fun `runNativeEventCallback should call post`() {
        miniAppWebView = spy(createMiniAppWebView())
        miniAppWebView.runNativeEventCallback(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
        verify(miniAppWebView).post(Mockito.any())
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
            miniAppFileChooser = miniAppFileChooser,
            hostAppUserAgentInfo = TEST_HA_NAME,
            miniAppWebChromeClient = webChromeClient,
            miniAppCustomPermissionCache = miniAppCustomPermissionCache,
            downloadedManifestCache = mock(),
            queryParams = TEST_URL_PARAMS,
            ratDispatcher = mock(),
            secureStorageDispatcher = mock(),
            enableH5Ads = false,
            miniAppIAPVerifier = mock()
        )
    }

    @Test
    fun `should remove cached permission data when window is closed`() {
        (miniAppWebView as MiniAppHttpWebView).callOnDetached()
        verify(miniAppCustomPermissionCache).removePermission(anyString())
    }
}

@Suppress("LargeClass")
@RunWith(AndroidJUnit4::class)
class MiniAppWebViewSpec : BaseWebViewSpec() {

    @Test
    fun `for a given app id, creates corresponding view for the caller`() {
        miniAppWebView.url!! shouldContain miniAppWebView.miniAppInfo.id
    }

    @Test
    fun `should start with corrected load url format`() {
        miniAppWebView.getLoadUrl() shouldBeEqualTo
                "https://mscheme.${miniAppWebView.miniAppInfo.id}/miniapp/index.html?$TEST_URL_PARAMS"
    }

    @Test
    fun `when MiniAppWebView is created then LayoutParams use MATCH_PARENT for dimensions`() {
        miniAppWebView.layoutParams.width shouldBeEqualTo ViewGroup.LayoutParams.MATCH_PARENT
        miniAppWebView.layoutParams.height shouldBeEqualTo ViewGroup.LayoutParams.MATCH_PARENT
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
            miniAppFileChooser = miniAppFileChooser,
            hostAppUserAgentInfo = "",
            miniAppWebChromeClient = webChromeClient,
            miniAppCustomPermissionCache = mock(),
            downloadedManifestCache = mock(),
            queryParams = TEST_URL_PARAMS,
            ratDispatcher = mock(),
            secureStorageDispatcher = mock(),
            enableH5Ads = false,
            miniAppIAPVerifier = mock()
        )
        miniAppWebView.settings.userAgentString shouldNotEndWith TEST_HA_NAME
    }

    /**
     * catching error due to test environment doesn't have gms library that has H5AdsProvider in it
     */
    @Test
    fun `should invoke MiniAppH5AdsProvider when isH5AdsEnabled is true`() {
        `when`(
            MiniAppWebView(
                context,
                basePath = basePath,
                miniAppInfo = TEST_MA,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = miniAppNavigator,
                miniAppFileChooser = miniAppFileChooser,
                hostAppUserAgentInfo = "",
                miniAppWebChromeClient = webChromeClient,
                miniAppCustomPermissionCache = mock(),
                downloadedManifestCache = mock(),
                queryParams = TEST_URL_PARAMS,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock(),
                enableH5Ads = true,
                miniAppIAPVerifier = mock()
            )
        ).itThrows(AssertionError())
    }

    @Test
    fun `when destroyView called then the MiniAppWebView should be disposed`() {
        val displayer = Mockito.spy(miniAppWebView)
        displayer.destroyView()

        verify(displayer).stopLoading()

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

        verify(displayer).onResume()
        verify(displayer).onPause()
        verify(displayer.miniAppWebChromeClient).onWebViewDetach()
        verify(miniAppMessageBridge).onWebViewDetach()
    }

    @Test
    @Suppress("LongMethod")
    fun `each mini app should have different domain`() {
        val miniAppWebViewForMiniapp1 = MiniAppWebView(
            context,
            miniAppWebView.basePath,
            TEST_MA,
            miniAppMessageBridge,
            miniAppNavigator,
            miniAppFileChooser,
            TEST_HA_NAME,
            mock(),
            mock(),
            mock(),
            TEST_URL_PARAMS,
            mock(),
            mock(),
            false,
            mock()
        )
        val miniAppWebViewForMiniapp2 = MiniAppWebView(
            context,
            miniAppWebView.basePath,
            TEST_MA.copy(id = "app-id-2"),
            miniAppMessageBridge,
            miniAppNavigator,
            miniAppFileChooser,
            TEST_HA_NAME,
            mock(),
            mock(),
            mock(),
            TEST_URL_PARAMS,
            mock(),
            mock(),
            false,
            mock()
        )
        miniAppWebViewForMiniapp1.url!! shouldNotBeEqualTo miniAppWebViewForMiniapp2.url!!
    }

    @Test
    fun `should load url which is internal mini app scheme from external emission`() {
        miniAppWebView.externalResultHandler.emitResult(TEST_URL_HTTPS_1)
        miniAppWebView.externalResultHandler.emitResult("mscheme.${miniAppWebView.miniAppInfo.id}://index.html")
        val intent = Intent().apply {
            putExtra(
                MiniAppExternalUrlLoader.returnUrlTag,
                "https://mscheme.${miniAppWebView.miniAppInfo.id}/miniapp/index.html"
            )
        }
        miniAppWebView.externalResultHandler.emitResult(intent)

        miniAppWebView.getLoadUrl() shouldBeEqualTo
                "https://mscheme.${miniAppWebView.miniAppInfo.id}/miniapp/index.html?$TEST_URL_PARAMS"
    }

    @Test(expected = InvalidUseOfMatchersException::class)
    fun `should send response for the specified ID over the mini app bridge`() {
        val spyMiniAppWebView = spy(miniAppWebView)
        spyMiniAppWebView.runSuccessCallback("test_id", "test_value")

        Verify on spyMiniAppWebView that spyMiniAppWebView.evaluateJavascript(
            argWhere { it.contains("""MiniAppBridge.execSuccessCallback(`test_id`""") }, null
        )
    }

    @Test(expected = InvalidUseOfMatchersException::class)
    fun `should send events for the specified event type over the mini app bridge`() {
        val spyMiniAppWebView = spy(miniAppWebView)
        spyMiniAppWebView.runNativeEventCallback("test_event_type", "test_value")

        Verify on spyMiniAppWebView that spyMiniAppWebView.evaluateJavascript(
            argWhere { it.contains("""MiniAppBridge.execCustomEventsCallback(`test_event_type`""") },
            null
        )
    }

    @Test(expected = InvalidUseOfMatchersException::class)
    fun `should send response with escaped backtick characters`() {
        val spyMiniAppWebView = spy(miniAppWebView)

        spyMiniAppWebView.runSuccessCallback("test_id", "`test response`")
        Verify on spyMiniAppWebView that spyMiniAppWebView.evaluateJavascript(
            argWhere { it.contains("""`\`test response\``""") }, null
        )

        spyMiniAppWebView.runErrorCallback("test_id", "`error response`")
        Verify on spyMiniAppWebView that spyMiniAppWebView.evaluateJavascript(
            argWhere { it.contains("""`\`error response\``""") }, null
        )
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
        val webViewClient = MiniAppWebViewClient(
            context, webAssetLoader, miniAppNavigator,
            externalResultHandler, miniAppScheme
        )
        val webResourceRequest = getWebResReq(TEST_URL_HTTPS_1.toUri())

        webViewClient.shouldInterceptRequest(miniAppWebView, webResourceRequest)

        verify(webAssetLoader!!).shouldInterceptRequest(webResourceRequest.url)
    }

    @Test
    fun `should redirect to custom domain when only loading with custom scheme`() {
        val webAssetLoader: WebViewAssetLoader? =
            (miniAppWebView.webViewClient as MiniAppWebViewClient).loader
        val customDomain = "https://mscheme.${miniAppWebView.miniAppInfo.id}/"
        val webViewClient = Mockito.spy(
            MiniAppWebViewClient(
                context, webAssetLoader, miniAppNavigator,
                externalResultHandler, miniAppScheme
            )
        )

        val displayer = Mockito.spy(miniAppWebView)

        webViewClient.onReceivedError(displayer, webResourceRequest, mock())
        webViewClient.onReceivedError(
            displayer,
            getWebResReq("mscheme.${miniAppWebView.miniAppInfo.id}://".toUri()), mock()
        )

        verify(webViewClient).loadWithCustomDomain(displayer, customDomain)
    }

    @Test
    fun `should open phone dialer when there is telephone scheme`() {
        val webAssetLoader: WebViewAssetLoader? =
            (miniAppWebView.webViewClient as MiniAppWebViewClient).loader
        val webViewClient = Mockito.spy(
            MiniAppWebViewClient(
                context, webAssetLoader, miniAppNavigator,
                externalResultHandler, miniAppScheme
            )
        )
        val displayer = Mockito.spy(miniAppWebView)

        webViewClient.shouldOverrideUrlLoading(displayer, webResourceRequest)
        webViewClient.shouldOverrideUrlLoading(displayer, getWebResReq(TEST_PHONE_URI.toUri()))

        verify(miniAppScheme).openPhoneDialer(context, TEST_PHONE_URI)
    }

    @Test
    fun `should open mail composer when there is mail scheme`() {
        val webAssetLoader: WebViewAssetLoader? =
            (miniAppWebView.webViewClient as MiniAppWebViewClient).loader
        val webViewClient = Mockito.spy(
            MiniAppWebViewClient(
                context, webAssetLoader, miniAppNavigator,
                externalResultHandler, miniAppScheme
            )
        )
        val displayer = Mockito.spy(miniAppWebView)

        webViewClient.shouldOverrideUrlLoading(displayer, webResourceRequest)
        webViewClient.shouldOverrideUrlLoading(displayer, getWebResReq(TEST_MAIL_URI.toUri()))

        verify(miniAppScheme).openMailComposer(context, TEST_MAIL_URI)
    }

    @Test
    fun `should have default external link handler when there is no config for navigation`() {
        val displayer = Mockito.spy(
            MiniAppWebView(
                context,
                basePath = basePath,
                miniAppInfo = TEST_MA,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = null,
                miniAppFileChooser = null,
                hostAppUserAgentInfo = TEST_HA_NAME,
                miniAppWebChromeClient = webChromeClient,
                miniAppCustomPermissionCache = miniAppCustomPermissionCache,
                downloadedManifestCache = mock(),
                queryParams = TEST_URL_PARAMS,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock(),
                enableH5Ads = false,
                miniAppIAPVerifier = mock()
            )
        )
        val miniAppNavigator = Mockito.spy(displayer.miniAppNavigator)
        val webAssetLoader: WebViewAssetLoader? =
            (displayer.webViewClient as MiniAppWebViewClient).loader
        val webViewClient = Mockito.spy(
            MiniAppWebViewClient(
                context, webAssetLoader, miniAppNavigator!!,
                externalResultHandler, miniAppScheme
            )
        )

        webViewClient.shouldOverrideUrlLoading(displayer, getWebResReq(TEST_URL_HTTPS_1.toUri()))

        miniAppNavigator shouldNotBe null
        verify(miniAppNavigator).openExternalUrl(TEST_URL_HTTPS_1, externalResultHandler)
    }

    @Test
    fun `should open external url loader when there is the config for navigation`() {
        val webAssetLoader: WebViewAssetLoader? =
            (miniAppWebView.webViewClient as MiniAppWebViewClient).loader
        val webViewClient = Mockito.spy(
            MiniAppWebViewClient(
                context, webAssetLoader, miniAppNavigator,
                externalResultHandler, miniAppScheme
            )
        )
        val displayer = Mockito.spy(miniAppWebView)

        webViewClient.shouldOverrideUrlLoading(displayer, getWebResReq(TEST_URL_HTTPS_1.toUri()))

        verify(miniAppNavigator).openExternalUrl(TEST_URL_HTTPS_1, externalResultHandler)
    }

    @Test
    fun `should not intercept mime type for regular cases`() {
        val webResourceResponse =
            WebResourceResponse("", "utf-8", ByteArrayInputStream("".toByteArray()))
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
        val webResourceResponse =
            WebResourceResponse("", "utf-8", ByteArrayInputStream("".toByteArray()))
        val webClient = miniAppWebView.webViewClient as MiniAppWebViewClient

        val request = getWebResReq("test.js".toUri())
        webClient.interceptMimeType(webResourceResponse, request)

        webResourceResponse.mimeType shouldBe "application/javascript"
    }
}

@RunWith(AndroidJUnit4::class)
class MiniAppWebChromeTest : BaseWebViewSpec() {

    @SuppressWarnings("ExpressionBodySyntax")
    private fun <T> spyLambda(lambdaType: Class<T>?, lambda: T): T {
        return Mockito.mock(lambdaType, delegatesTo<Any>(lambda))
    }

    @Test
    fun `for a WebChromeClient, it should be MiniAppWebChromeClient`() {
        miniAppWebView.webChromeClient shouldBeInstanceOf MiniAppWebChromeClient::class
    }

    @Test
    fun `should do js injection when there is a change in the document title`() {
        val webChromeClient = Mockito.spy(miniAppWebView.webChromeClient as MiniAppWebChromeClient)
        webChromeClient.onReceivedTitle(miniAppWebView, "web_title")

        verify(webChromeClient).doInjection(miniAppWebView)
    }

    @Test
    fun `bridge js should be null when js asset is inaccessible`() {
        val webClient = MiniAppWebChromeClient(mock(), TEST_MA, mock(), mock(), mock())
        webClient.bridgeJs shouldBe null
    }

    @Test
    fun `should allow geolocation callback when custom permission is allowed`() {
        doReturn(true).whenever(miniAppCustomPermissionCache)
            .hasPermission(TEST_MA_ID, MiniAppCustomPermissionType.LOCATION)

        val geoLocationCallback = spyLambda(
            GeolocationPermissions.Callback::class.java,
            GeolocationPermissions.Callback { _, allow, retain ->
                allow shouldBe true
                retain shouldBe false
            }
        )
        webChromeClient.onGeolocationPermissionsShowPrompt("", geoLocationCallback)

        verify(geoLocationCallback).invoke("", true, false)
    }

    @Test
    fun `should not allow geolocation callback when custom permission is denied`() {
        doReturn(false).whenever(miniAppCustomPermissionCache)
            .hasPermission(TEST_MA_ID, MiniAppCustomPermissionType.LOCATION)

        val geoLocationCallback = spyLambda(
            GeolocationPermissions.Callback::class.java,
            GeolocationPermissions.Callback { _, allow, retain ->
                allow shouldBe false
                retain shouldBe false
            }
        )
        webChromeClient.onGeolocationPermissionsShowPrompt("", geoLocationCallback)

        verify(geoLocationCallback).invoke("", false, false)
    }

    @Test
    fun `should override js dialog event`() {
        webChromeClient.onJsAlert(
            miniAppWebView, TEST_URL_HTTPS_2, TEST_BODY_CONTENT, mock()
        ) shouldBe true
        webChromeClient.onJsConfirm(
            miniAppWebView, TEST_URL_HTTPS_2, TEST_BODY_CONTENT, mock()
        ) shouldBe true
        webChromeClient.onJsPrompt(
            miniAppWebView, TEST_URL_HTTPS_2, TEST_BODY_CONTENT, TEST_VALUE, mock()
        ) shouldBe true
        webChromeClient.onJsPrompt(
            miniAppWebView, TEST_URL_HTTPS_2, TEST_BODY_CONTENT, null, mock()
        ) shouldBe true
    }

    @Test
    fun `should close custom view when exit`() {
        val context = getApplicationContext<Context>()
        webChromeClient =
            Mockito.spy(MiniAppWebChromeClient(context, TEST_MA, mock(), mock(), mock()))
        webChromeClient.onShowCustomView(null, mock())
        webChromeClient.customView = mock()
        webChromeClient.onShowCustomView(mock(), mock())

        verify(webChromeClient).onHideCustomView()
    }

    @Test
    fun `should execute custom view flow without error`() {
        val webChromeClient =
            Mockito.spy(MiniAppWebChromeClient(context, TEST_MA, mock(), mock(), mock()))

        webChromeClient.onShowCustomView(View(context), mock())
        webChromeClient.updateControls()
        webChromeClient.onHideCustomView()
        webChromeClient.updateControls()
    }

    @Test
    fun `should exit fullscreen when destroy miniapp view`() {
        val webChromeClient =
            Mockito.spy(MiniAppWebChromeClient(context, TEST_MA, mock(), mock(), mock()))
        webChromeClient.onShowCustomView(View(context), mock())
        webChromeClient.onWebViewDetach()

        verify(webChromeClient).onHideCustomView()
    }

    @Test
    fun `onShowFileChooser should invoke from miniapp file chooser`() {
        val callback: ValueCallback<Array<Uri>>? = mock()
        val fileChooserParams: WebChromeClient.FileChooserParams? = mock()
        val webChromeClient =
            Mockito.spy(
                MiniAppWebChromeClient(
                    context,
                    TEST_MA,
                    mock(),
                    miniAppFileChooser,
                    mock()
                )
            )
        webChromeClient.onShowFileChooser(miniAppWebView, callback, fileChooserParams)
        verify(miniAppFileChooser).onShowFileChooser(callback, fileChooserParams, context)
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
