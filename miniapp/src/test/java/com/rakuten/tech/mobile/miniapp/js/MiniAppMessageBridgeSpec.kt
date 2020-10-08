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
import com.rakuten.tech.mobile.miniapp.ads.TestAdMobDisplayer
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.permission.*
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@Suppress("TooGenericExceptionThrown")
open class BridgeCommon {
    internal val webViewListener: WebViewListener = mock()
    internal val bridgeExecutor = Mockito.spy(BridgeExecutor(webViewListener))

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
                onRequestCustomPermissionsResult(TEST_CALLBACK_ID, TEST_CUSTOM_PERMISSION_RESULT)
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

    protected fun createDefaultMiniAppMessageBridge(): MiniAppMessageBridge = object : MiniAppMessageBridge() {
        override fun getUniqueId() = TEST_CALLBACK_VALUE

        override fun requestPermission(
            miniAppPermissionType: MiniAppPermissionType,
            callback: (isGranted: Boolean) -> Unit
        ) {
            onRequestPermissionsResult(TEST_CALLBACK_ID, false)
        }
    }

    internal fun createErrorWebViewListener(errMsg: String): WebViewListener =
        object : WebViewListener {
            override fun runSuccessCallback(callbackId: String, value: String) {
                throw Exception()
            }

            override fun runErrorCallback(callbackId: String, errorMessage: String) {
                Assert.assertEquals(errorMessage, errMsg)
            }
        }
}

@RunWith(AndroidJUnit4::class)
class MiniAppMessageBridgeSpec : BridgeCommon() {
    private val miniAppBridge: MiniAppMessageBridge = Mockito.spy(
        createMiniAppMessageBridge(false)
    )

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

    @Before
    fun setup() {
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
    }

    @Test
    fun `getUniqueId should be called when there is a getting unique id request from external`() {
        miniAppBridge.postMessage(uniqueIdJsonStr)

        verify(miniAppBridge, times(1)).getUniqueId()
        verify(bridgeExecutor, times(1)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `postValue should be called when permission is granted`() {
        val isPermissionGranted = true
        val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(isPermissionGranted))
        val webViewListener = createErrorWebViewListener("${ErrorBridgeMessage.ERR_REQ_PERMISSION} null")
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )

        miniAppBridge.postMessage(permissionJsonStr)

        verify(bridgeExecutor, times(1))
            .postValue(permissionCallbackObj.id, MiniAppPermissionResult.getValue(isPermissionGranted).type)
    }

    @Test
    fun `postError should be called when permission is denied`() {
        miniAppBridge.postMessage(permissionJsonStr)

        verify(bridgeExecutor, times(1))
            .postError(permissionCallbackObj.id, MiniAppPermissionResult.getValue(false).type)
    }

    @Test
    fun `postValue should not be called when calling postError`() {
        bridgeExecutor.postError(TEST_CALLBACK_ID, TEST_ERROR_MSG)

        verify(bridgeExecutor, times(0)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `postError should be called when cannot get unique id`() {
        val errMsg = "Cannot get unique id: null"
        val webViewListener = createErrorWebViewListener("${ErrorBridgeMessage.ERR_UNIQUE_ID} null")
        val bridgeExecutor = Mockito.spy(miniAppBridge.createBridgeExecutor(webViewListener))
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        miniAppBridge.postMessage(uniqueIdJsonStr)

        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when requestCustomPermissions hasn't been implemented`() {
        val errMsg =
            "Cannot request custom permissions: The `MiniAppMessageBridge.requestCustomPermissions`" +
                    " method has not been implemented by the Host App."
        val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
        val bridgeExecutor = Mockito.spy(miniAppBridge.createBridgeExecutor(webViewListener))
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        miniAppBridge.postMessage(customPermissionJsonStr)

        verify(bridgeExecutor, times(1)).postError(customPermissionCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when can request custom permission`() {
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )

        miniAppBridge.postMessage(customPermissionJsonStr)

        verify(bridgeExecutor, times(1))
            .postValue(customPermissionCallbackObj.id, TEST_CUSTOM_PERMISSION_RESULT)
    }
}

@RunWith(AndroidJUnit4::class)
class ShareContentBridgeSpec : BridgeCommon() {
    val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())

    @Before
    fun setupShareInfo() {
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
    }

    private val shareContentJsonStr = createShareCallbackJsonStr("This is content")

    private fun createShareCallbackJsonStr(content: String) = Gson().toJson(
            CallbackObj(
                action = ActionType.SHARE_INFO.action,
                param = ShareInfoCallbackObj.ShareInfoParam(
                    ShareInfo(content)
                ),
                id = TEST_CALLBACK_ID
            )
    )

    @Test
    fun `postValue should be called when content is shared successfully`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                miniAppInfo = mock()
            )
            miniAppBridge.postMessage(shareContentJsonStr)

            verify(bridgeExecutor, times(1)).postValue(TEST_CALLBACK_ID, SUCCESS)
        }
    }

    @Test
    fun `postError should be called when cannot share content`() {
        val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(false))
        val errMsg = "${ErrorBridgeMessage.ERR_SHARE_CONTENT} null"
        val webViewListener = createErrorWebViewListener(errMsg)
        val bridgeExecutor = Mockito.spy(miniAppBridge.createBridgeExecutor(webViewListener))
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        miniAppBridge.postMessage(shareContentJsonStr)

        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postValue should not be called when using default method without Activity`() {
        miniAppBridge.postMessage(shareContentJsonStr)

        verify(bridgeExecutor, times(0)).postValue(TEST_CALLBACK_ID, SUCCESS)
    }

    @Test
    fun `postValue should not be called when sharing empty content`() {
        val shareContentJsonStr = createShareCallbackJsonStr(" ")
        miniAppBridge.postMessage(shareContentJsonStr)

        verify(bridgeExecutor, times(0)).postValue(TEST_CALLBACK_ID, SUCCESS)
    }
}

class AdBridgeSpec : BridgeCommon() {
    private lateinit var miniAppBridge: MiniAppMessageBridge
    private lateinit var miniAppBridgeWithAdMob: MiniAppMessageBridge

    @Before
    fun setupAd() {
        miniAppBridge = initMiniAppBridge("non.existence.class")
        miniAppBridgeWithAdMob = initMiniAppBridge("org.junit.Assert")
    }

    private fun createAdJsonStr(action: String, adType: Int, adUnitId: String) = Gson().toJson(
        CallbackObj(
            action = action,
            param = AdObj(adType, adUnitId),
            id = TEST_CALLBACK_ID
        )
    )

    private fun initMiniAppBridge(adMobCheckedClass: String): MiniAppMessageBridge {
        AdMobClassName = adMobCheckedClass
        val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        miniAppBridge.setAdMobDisplayer(TestAdMobDisplayer())
        return miniAppBridge
    }

    @Test
    fun `postError should be called when AdMob is not provided`() {
        var errMsg = "${ErrorBridgeMessage.ERR_LOAD_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}"
        var jsonStr = createAdJsonStr(ActionType.LOAD_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        miniAppBridge.postMessage(jsonStr)
        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errMsg)

        jsonStr = createAdJsonStr(ActionType.SHOW_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        errMsg = "${ErrorBridgeMessage.ERR_SHOW_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}"
        miniAppBridge.postMessage(jsonStr)
        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when cannot load interstitial`() {
        val errMsg = "${ErrorBridgeMessage.ERR_LOAD_AD} null"
        val jsonStr = createAdJsonStr(ActionType.LOAD_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        miniAppBridgeWithAdMob.postMessage(jsonStr)

        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when cannot show interstitial`() {
        val errMsg = "${ErrorBridgeMessage.ERR_SHOW_AD} null"
        val jsonStr = createAdJsonStr(ActionType.SHOW_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        miniAppBridgeWithAdMob.postMessage(jsonStr)

        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errMsg)
    }
}
