package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_AD_UNIT_ID
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_VALUE
import com.rakuten.tech.mobile.miniapp.TEST_ERROR_MSG
import com.rakuten.tech.mobile.miniapp.ads.AdMobClassName
import com.rakuten.tech.mobile.miniapp.ads.MiniAppAdDisplayer
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.permission.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@Suppress("TooGenericExceptionThrown")
@RunWith(AndroidJUnit4::class)
open class MiniAppMessageBridgeSpec {
    private val miniAppBridge: MiniAppMessageBridge = Mockito.spy(
        createMiniAppMessageBridge(false)
    )

    protected fun createMiniAppMessageBridge(isPermissionGranted: Boolean): MiniAppMessageBridge =
        object : MiniAppMessageBridge() {
            override fun getUniqueId() = TEST_CALLBACK_VALUE

            override fun requestPermission(
                miniAppPermissionType: MiniAppPermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) {
                onRequestPermissionsResult(TEST_CALLBACK_ID, isPermissionGranted)
            }

            override fun requestCustomPermissions(
                permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
                callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
            ) {
                val grantResult = "{\"rakuten.miniapp.user.USER_NAME\":\"DENIED\"}"
                onRequestCustomPermissionsResult(TEST_CALLBACK_ID, grantResult)
            }

            override fun shareContent(
                content: String,
                callback: (isSuccess: Boolean, message: String?) -> Unit
            ) {
                callback.invoke(true, SUCCESS)
                callback.invoke(false, null)
                callback.invoke(false, TEST_ERROR_MSG)
            }
        }

    protected fun createDefaultMiniAppMessageBridge(
        miniAppAdDisplayer: MiniAppAdDisplayer? = null
    ): MiniAppMessageBridge =
        object : MiniAppMessageBridge(miniAppAdDisplayer) {
            override fun getUniqueId() = TEST_CALLBACK_VALUE

            override fun requestPermission(
                miniAppPermissionType: MiniAppPermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) {
                onRequestPermissionsResult(TEST_CALLBACK_ID, false)
            }

            override fun requestCustomPermissions(
                permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
                callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
            ) {
                val grantResult = "{\"rakuten.miniapp.user.USER_NAME\":\"DENIED\"}"
                onRequestCustomPermissionsResult(TEST_CALLBACK_ID, grantResult)
            }
        }

    private val uniqueIdCallbackObj = CallbackObj(
        action = ActionType.GET_UNIQUE_ID.action,
        param = null,
        id = TEST_CALLBACK_ID)
    private val uniqueIdJsonStr = Gson().toJson(uniqueIdCallbackObj)

    private val permissionCallbackObj = CallbackObj(
        action = ActionType.REQUEST_PERMISSION.action,
        param = Gson().toJson(Permission(MiniAppPermissionType.LOCATION.type)),
        id = TEST_CALLBACK_ID)
    private val permissionJsonStr = Gson().toJson(permissionCallbackObj)

    private val customPermissionCallbackObj = CustomPermissionCallbackObj(
        action = ActionType.REQUEST_CUSTOM_PERMISSIONS.action,
        param = CustomPermission(
            listOf(CustomPermissionObj(MiniAppCustomPermissionType.USER_NAME.type, ""))
        ),
        id = TEST_CALLBACK_ID
    )
    private val customPermissionJsonStr = Gson().toJson(customPermissionCallbackObj)

    internal fun createErrorWebViewListener(errMsg: String): WebViewListener =
        object : WebViewListener {
            override fun runSuccessCallback(callbackId: String, value: String) {
                throw Exception()
            }

            override fun runErrorCallback(callbackId: String, errorMessage: String) {
                Assert.assertEquals(errorMessage, errMsg)
            }
        }

    @Before
    fun setup() {
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = mock(),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
    }

    @Test
    fun `getUniqueId should be called when there is a getting unique id request from external`() {
        miniAppBridge.postMessage(uniqueIdJsonStr)

        verify(miniAppBridge, times(1)).getUniqueId()
        verify(miniAppBridge, times(1)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `postValue should be called when permission is granted`() {
        val isPermissionGranted = true
        val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(isPermissionGranted))
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = createErrorWebViewListener("${ErrorBridgeMessage.ERR_REQ_PERMISSION} null"),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )

        miniAppBridge.postMessage(permissionJsonStr)

        verify(miniAppBridge, times(1))
            .postValue(permissionCallbackObj.id, MiniAppPermissionResult.getValue(isPermissionGranted).type)
    }

    @Test
    fun `postError should be called when permission is denied`() {
        miniAppBridge.postMessage(permissionJsonStr)

        verify(miniAppBridge, times(1))
            .postError(permissionCallbackObj.id, MiniAppPermissionResult.getValue(false).type)
    }

    @Test
    fun `postValue should not be called when calling postError`() {
        miniAppBridge.postError(TEST_CALLBACK_ID, TEST_ERROR_MSG)

        verify(miniAppBridge, times(0)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `postError should be called when cannot get unique id`() {
        val errMsg = "Cannot get unique id: null"
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = createErrorWebViewListener("${ErrorBridgeMessage.ERR_UNIQUE_ID} null"),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        miniAppBridge.postMessage(uniqueIdJsonStr)

        verify(miniAppBridge, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postValue should be called when can request custom permission`() {
        val isPermissionGranted = false
        val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(isPermissionGranted))
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = createErrorWebViewListener("${ErrorBridgeMessage.ERR_REQ_CUSTOM_PERMISSION} null"),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )

        miniAppBridge.postMessage(customPermissionJsonStr)

        verify(miniAppBridge, times(1))
            .postValue(customPermissionCallbackObj.id, "{\"rakuten.miniapp.user.USER_NAME\":\"DENIED\"}")
    }

    @Test
    fun `postError should be called when cannot request custom permission`() {
        val errMsg = "${ErrorBridgeMessage.ERR_REQ_CUSTOM_PERMISSION} null"
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = createErrorWebViewListener(errMsg),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        miniAppBridge.postMessage(customPermissionJsonStr)

        verify(miniAppBridge, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }
}

class ShareContentBridgeSpec : MiniAppMessageBridgeSpec() {
    val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())

    @Before
    fun setupShareInfo() {
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = mock(),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
    }

    private val shareContentCallbackObj = createShareCallbackObj("This is content")
    private val shareContentJsonStr = Gson().toJson(shareContentCallbackObj)

    private fun createShareCallbackObj(content: String) = CallbackObj(
        action = ActionType.SHARE_INFO.action,
        param = ShareInfoCallbackObj.ShareInfoParam(
            ShareInfo(content)
        ),
        id = TEST_CALLBACK_ID
    )

    @Test
    fun `postValue should be called when content is shared successfully`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            miniAppBridge.init(
                activity = activity,
                webViewListener = mock(),
                customPermissionCache = mock(),
                miniAppInfo = mock()
            )
            miniAppBridge.postMessage(shareContentJsonStr)

            verify(miniAppBridge, times(1)).postValue(TEST_CALLBACK_ID, SUCCESS)
        }
    }

    @Test
    fun `postError should be called when cannot share content`() {
        val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(false))
        val errMsg = "${ErrorBridgeMessage.ERR_SHARE_CONTENT} null"
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = createErrorWebViewListener(errMsg),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        miniAppBridge.postMessage(shareContentJsonStr)

        verify(miniAppBridge, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postValue should not be called when using default method without Activity`() {
        miniAppBridge.postMessage(shareContentJsonStr)

        verify(miniAppBridge, times(0)).postValue(TEST_CALLBACK_ID, SUCCESS)
    }

    @Test
    fun `postValue should not be called when sharing empty content`() {
        val shareContentJsonStr = Gson().toJson(createShareCallbackObj(" "))
        miniAppBridge.postMessage(shareContentJsonStr)

        verify(miniAppBridge, times(0)).postValue(TEST_CALLBACK_ID, SUCCESS)
    }
}

@Suppress("TooGenericExceptionThrown")
class AdBridgeSpec : MiniAppMessageBridgeSpec() {
    val adDisplayer = object : MiniAppAdDisplayer {
        override fun loadInterstitial(
            adUnitId: String,
            onLoaded: () -> Unit,
            onFailed: (String) -> Unit
        ) {
            throw Exception()
        }

        override fun showInterstitial(
            adUnitId: String,
            onClosed: () -> Unit,
            onFailed: (String) -> Unit
        ) {
            throw Exception()
        }
    }
    val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge(adDisplayer))

    @Before
    fun setupAd() {
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = mock(),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        AdMobClassName = "org.junit.Assert" // AdMob is provided.
    }

    private val interestitialAdCallbackObj = createAdCallbackObj(
        ActionType.LOAD_AD.action,
        AdType.INTERSTITIAL.value,
        TEST_AD_UNIT_ID
    )
    private val interestitialAdJsonStr = Gson().toJson(interestitialAdCallbackObj)

    private fun createAdCallbackObj(action: String, adType: Int, adUnitId: String) = CallbackObj(
        action = action,
        param = AdObj(adType, adUnitId),
        id = TEST_CALLBACK_ID
    )

    @Test
    fun `postError should be called when AdMob is not provided`() {
        var errMsg = "${ErrorBridgeMessage.ERR_LOAD_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}"
        AdMobClassName = "non.existence.class"
        miniAppBridge.postMessage(interestitialAdJsonStr)

        verify(miniAppBridge).postError(TEST_CALLBACK_ID, errMsg)

        val showAdCallbackObj = createAdCallbackObj(
            ActionType.SHOW_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID
        )
        val jsonStr = Gson().toJson(showAdCallbackObj)
        errMsg = "${ErrorBridgeMessage.ERR_SHOW_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}"
        miniAppBridge.postMessage(jsonStr)

        verify(miniAppBridge).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when cannot load interstitial`() {
        val errMsg = "${ErrorBridgeMessage.ERR_LOAD_AD} null"
        miniAppBridge.postMessage(interestitialAdJsonStr)

        verify(miniAppBridge).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when cannot show interstitial`() {
        val errMsg = "${ErrorBridgeMessage.ERR_SHOW_AD} null"
        val interestitialAdCallbackObj = createAdCallbackObj(
            ActionType.SHOW_AD.action,
            AdType.INTERSTITIAL.value,
            TEST_AD_UNIT_ID
        )
        val interestitialAdJsonStr = Gson().toJson(interestitialAdCallbackObj)
        miniAppBridge.postMessage(interestitialAdJsonStr)

        verify(miniAppBridge).postError(TEST_CALLBACK_ID, errMsg)
    }
}
