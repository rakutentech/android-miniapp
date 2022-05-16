package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_AD_UNIT_ID
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_VALUE
import com.rakuten.tech.mobile.miniapp.TEST_ERROR_MSG
import com.rakuten.tech.mobile.miniapp.AdMobClassName
import com.rakuten.tech.mobile.miniapp.ads.TestAdMobDisplayer
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.ERR_GET_ENVIRONMENT_INFO
import com.rakuten.tech.mobile.miniapp.js.hostenvironment.HostEnvironmentInfo
import com.rakuten.tech.mobile.miniapp.js.hostenvironment.HostEnvironmentInfoError
import com.rakuten.tech.mobile.miniapp.permission.*
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import kotlin.test.assertEquals

@Suppress("TooGenericExceptionThrown")
open class BridgeCommon {
    internal val webViewListener: WebViewListener = mock()
    internal val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))

    protected fun createMiniAppMessageBridge(
        isPermissionGranted: Boolean,
        hasEnvInfo: Boolean = false
    ): MiniAppMessageBridge =
        object : MiniAppMessageBridge() {

            override fun getUniqueId(
                onSuccess: (uniqueId: String) -> Unit,
                onError: (message: String) -> Unit
            ) {
                onSuccess(TEST_CALLBACK_VALUE)
            }

            override fun getMessagingUniqueId(
                onSuccess: (uniqueId: String) -> Unit,
                onError: (message: String) -> Unit
            ) {
                onSuccess(TEST_CALLBACK_VALUE)
            }

            override fun getMauid(
                onSuccess: (mauId: String) -> Unit,
                onError: (message: String) -> Unit
            ) {
                onSuccess(TEST_CALLBACK_VALUE)
            }

            override fun requestDevicePermission(
                miniAppPermissionType: MiniAppDevicePermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) {
                onRequestDevicePermissionsResult(TEST_CALLBACK_ID, isPermissionGranted)
            }

            override fun shareContent(
                content: String,
                callback: (isSuccess: Boolean, message: String?) -> Unit
            ) {
                callback.invoke(true, SUCCESS)
                callback.invoke(false, null)
                callback.invoke(false, TEST_ERROR_MSG)
            }

            override fun getHostEnvironmentInfo(
                onSuccess: (info: HostEnvironmentInfo) -> Unit,
                onError: (infoError: HostEnvironmentInfoError) -> Unit
            ) {
                if (hasEnvInfo) {
                    ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
                        onSuccess.invoke(HostEnvironmentInfo(activity, "en"))
                    }
                } else {
                    val infoErrMessage = "{\"type\":\"$ERR_GET_ENVIRONMENT_INFO $TEST_ERROR_MSG\"}"
                    onError.invoke(HostEnvironmentInfoError(infoErrMessage))
                }
            }
        }

    protected fun createDefaultMiniAppMessageBridge(): MiniAppMessageBridge = object : MiniAppMessageBridge() {

        override fun getUniqueId(
            onSuccess: (uniqueId: String) -> Unit,
            onError: (message: String) -> Unit
        ) {
            onSuccess(TEST_CALLBACK_VALUE)
        }

        override fun getMessagingUniqueId(
            onSuccess: (uniqueId: String) -> Unit,
            onError: (message: String) -> Unit
        ) {
            onSuccess(TEST_CALLBACK_VALUE)
        }

        override fun getMauid(
            onSuccess: (mauId: String) -> Unit,
            onError: (message: String) -> Unit
        ) {
            onSuccess(TEST_CALLBACK_VALUE)
        }

        override fun requestDevicePermission(
            miniAppPermissionType: MiniAppDevicePermissionType,
            callback: (isGranted: Boolean) -> Unit
        ) {
            onRequestDevicePermissionsResult(TEST_CALLBACK_ID, false)
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

            override fun runNativeEventCallback(eventType: String, value: String) {
                throw Exception()
            }
        }
}

@RunWith(AndroidJUnit4::class)
@Suppress("LongMethod", "LargeClass")
class MiniAppMessageBridgeSpec : BridgeCommon() {
    private val miniAppBridge: MiniAppMessageBridge = Mockito.spy(
        createMiniAppMessageBridge(false)
    )

    private val uniqueIdCallbackObj = CallbackObj(
        action = ActionType.GET_UNIQUE_ID.action,
        param = null,
        id = TEST_CALLBACK_ID)
    private val uniqueIdJsonStr = Gson().toJson(uniqueIdCallbackObj)

    private val messagingUniqueIdCallbackObj = CallbackObj(
        action = ActionType.GET_MESSAGING_UNIQUE_ID.action,
        param = null,
        id = TEST_CALLBACK_ID)
    private val messagingUniqueIdJsonStr = Gson().toJson(messagingUniqueIdCallbackObj)

    private val mauIdCallbackObj = CallbackObj(
        action = ActionType.GET_MAUID.action,
        param = null,
        id = TEST_CALLBACK_ID)
    private val mauIdJsonStr = Gson().toJson(mauIdCallbackObj)

    private val permissionCallbackObj = CallbackObj(
        action = ActionType.REQUEST_PERMISSION.action,
        param = Gson().toJson(DevicePermission(MiniAppDevicePermissionType.LOCATION.type)),
        id = TEST_CALLBACK_ID)
    private val permissionJsonStr = Gson().toJson(permissionCallbackObj)

    private val hostEnvInfoCallbackObj = CallbackObj(
        action = ActionType.GET_HOST_ENVIRONMENT_INFO.action,
        param = null,
        id = TEST_CALLBACK_ID
    )

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
        }
    }

    @Test
    fun `should be able to return unique id to miniapp`() {
        miniAppBridge.postMessage(uniqueIdJsonStr)

        verify(bridgeExecutor).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `postError should be called when cannot get unique id`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val errMsg = "Cannot get unique id: null"
            val webViewListener = createErrorWebViewListener("${ErrorBridgeMessage.ERR_UNIQUE_ID} null")
            val bridgeExecutor = Mockito.spy(miniAppBridge.createBridgeExecutor(webViewListener))
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            miniAppBridge.postMessage(uniqueIdJsonStr)

            verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errMsg)
        }
    }

    @Test
    fun `should be able to return contact id to miniapp`() {
        miniAppBridge.postMessage(messagingUniqueIdJsonStr)

        verify(bridgeExecutor).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `postError should be called when cannot get contact id`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val errMsg = "Cannot get messaging unique id: null"
            val webViewListener =
                createErrorWebViewListener("${ErrorBridgeMessage.ERR_MESSAGING_UNIQUE_ID} null")
            val bridgeExecutor = Mockito.spy(miniAppBridge.createBridgeExecutor(webViewListener))
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            miniAppBridge.postMessage(messagingUniqueIdJsonStr)

            verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errMsg)
        }
    }

    @Test
    fun `should be able to return mauid to miniapp`() {
        miniAppBridge.postMessage(mauIdJsonStr)

        verify(bridgeExecutor).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `postError should be called when cannot get mauid`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->

            val errMsg = "Cannot get mauid: null"
            val webViewListener = createErrorWebViewListener("${ErrorBridgeMessage.ERR_MAUID} null")
            val bridgeExecutor = Mockito.spy(miniAppBridge.createBridgeExecutor(webViewListener))
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            miniAppBridge.postMessage(mauIdJsonStr)

            verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errMsg)
        }
    }

    /** region: device permission */
    @Test
    fun `postError should be called when device permission granted is false`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(false))
            val errMsg = "Denied"
            val webViewListener = createErrorWebViewListener("dummy message")
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )

            miniAppBridge.postMessage(permissionJsonStr)

            verify(bridgeExecutor).postError(permissionCallbackObj.id, errMsg)
        }
    }

    @Test
    fun `postValue should be called when device permission is granted`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val isPermissionGranted = true
            val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(isPermissionGranted))
            val webViewListener =
                createErrorWebViewListener("${ErrorBridgeMessage.ERR_REQ_DEVICE_PERMISSION} null")
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )

            miniAppBridge.postMessage(permissionJsonStr)

            verify(bridgeExecutor)
                .postValue(
                    permissionCallbackObj.id,
                    MiniAppDevicePermissionResult.getValue(isPermissionGranted).type
                )
        }
    }

    @Test
    fun `postError should be called when device permission is denied`() {
        miniAppBridge.postMessage(permissionJsonStr)

        verify(bridgeExecutor)
            .postError(permissionCallbackObj.id, MiniAppDevicePermissionResult.getValue(false).type)
    }
    /** end region */

    @Test
    fun `postValue should not be called when calling postError`() {
        bridgeExecutor.postError(TEST_CALLBACK_ID, TEST_ERROR_MSG)

        verify(bridgeExecutor, times(0)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    /** region: host environment info */
    @Test
    fun `postError should be called when error callback invoked to get host environment info`() {
        val errMsg = "{\"type\":\"{\\\"type\\\":\\\"Cannot get host environment info: error_message\\\"}\"}"
        miniAppBridge.onGetHostEnvironmentInfo(hostEnvInfoCallbackObj.id)
        verify(bridgeExecutor).postError(hostEnvInfoCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when success callback invoked to get host environment info`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(false, true))
            val webViewListener = createErrorWebViewListener("${ErrorBridgeMessage.ERR_REQ_DEVICE_PERMISSION} null")
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            val info = HostEnvironmentInfo(activity, "en")
            miniAppBridge.onGetHostEnvironmentInfo(hostEnvInfoCallbackObj.id)
            verify(bridgeExecutor).postValue(hostEnvInfoCallbackObj.id, Gson().toJson(info))
        }
    }
    /** end region */

    @Test
    fun `all error bridge messages should be expected`() {
        assertEquals("no implementation by the Host App.", ErrorBridgeMessage.NO_IMPL)
        assertEquals("No support from hostapp", ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP)
        assertEquals("Cannot get unique id:", ErrorBridgeMessage.ERR_UNIQUE_ID)
        assertEquals("Cannot request device permission:", ErrorBridgeMessage.ERR_REQ_DEVICE_PERMISSION)
        assertEquals("Cannot request custom permissions:", ErrorBridgeMessage.ERR_REQ_CUSTOM_PERMISSION)
        assertEquals(
            "The `MiniAppMessageBridge.requestDevicePermission` ${ErrorBridgeMessage.NO_IMPL}",
            ErrorBridgeMessage.NO_IMPLEMENT_DEVICE_PERMISSION
        )
        assertEquals(
            "The `MiniAppMessageBridge.requestCustomPermissions` ${ErrorBridgeMessage.NO_IMPL}",
            ErrorBridgeMessage.NO_IMPLEMENT_CUSTOM_PERMISSION
        )
        assertEquals("Cannot share content:", ErrorBridgeMessage.ERR_SHARE_CONTENT)
        assertEquals("Cannot load ad:", ErrorBridgeMessage.ERR_LOAD_AD)
        assertEquals("Cannot show ad:", ErrorBridgeMessage.ERR_SHOW_AD)
        assertEquals("Cannot request screen action:", ErrorBridgeMessage.ERR_SCREEN_ACTION)
        assertEquals("Cannot get host environment info:", ERR_GET_ENVIRONMENT_INFO)
    }
}

@RunWith(AndroidJUnit4::class)
class ShareContentBridgeSpec : BridgeCommon() {
    private val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())

    @Before
    fun setupShareInfo() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
        }
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
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            miniAppBridge.postMessage(shareContentJsonStr)

            verify(bridgeExecutor).postValue(TEST_CALLBACK_ID, SUCCESS)
        }
    }

    @Test
    fun `dispatchEvent should not be called if bridge executor is not initialized`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            // When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            miniAppBridge.dispatchNativeEvent(NativeEventType.EXTERNAL_WEBVIEW_CLOSE, "")

            verify(bridgeExecutor, times(0))
                .dispatchEvent(NativeEventType.EXTERNAL_WEBVIEW_CLOSE.value, "")
        }
    }

    @Test
    fun `dispatchEvent should  be called if bridge executor is not initialized`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            miniAppBridge.dispatchNativeEvent(NativeEventType.EXTERNAL_WEBVIEW_CLOSE, "")

            verify(bridgeExecutor).dispatchEvent(NativeEventType.EXTERNAL_WEBVIEW_CLOSE.value, "")
        }
    }

    @Test
    fun `native event type should return the correct value`() {
        NativeEventType.EXTERNAL_WEBVIEW_CLOSE.value shouldBeEqualTo "miniappwebviewclosed"
        NativeEventType.MINIAPP_ON_PAUSE.value shouldBeEqualTo "miniapppause"
        NativeEventType.MINIAPP_ON_RESUME.value shouldBeEqualTo "miniappresume"
    }

    @Test
    fun `postError should be called when cannot share content`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(false))
            val errMsg = "${ErrorBridgeMessage.ERR_SHARE_CONTENT} null"
            val webViewListener = createErrorWebViewListener(errMsg)
            val bridgeExecutor = Mockito.spy(miniAppBridge.createBridgeExecutor(webViewListener))
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            miniAppBridge.postMessage(shareContentJsonStr)

            verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errMsg)
        }
    }

    @Test
    fun `postValue should not be called when using default method without Activity`() {
        miniAppBridge.init(
            activity = TestFilesDirActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            downloadedManifestCache = mock(),
            miniAppId = TEST_MA_ID,
            ratDispatcher = mock(),
            secureStorageDispatcher = mock()
        )
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

@RunWith(AndroidJUnit4::class)
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
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
        }
        miniAppBridge.setAdMobDisplayer(TestAdMobDisplayer())
        return miniAppBridge
    }

    @Test
    fun `postError should be called when AdMob is not provided`() {
        var errMsg = "${ErrorBridgeMessage.ERR_LOAD_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}"
        var jsonStr = createAdJsonStr(ActionType.LOAD_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        miniAppBridge.postMessage(jsonStr)
        jsonStr = createAdJsonStr(ActionType.LOAD_AD.action, AdType.REWARDED.value, TEST_AD_UNIT_ID)
        miniAppBridge.postMessage(jsonStr)
        verify(bridgeExecutor, times(2)).postError(TEST_CALLBACK_ID, errMsg)

        jsonStr = createAdJsonStr(ActionType.SHOW_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        errMsg = "${ErrorBridgeMessage.ERR_SHOW_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}"
        miniAppBridge.postMessage(jsonStr)
        jsonStr = createAdJsonStr(ActionType.SHOW_AD.action, AdType.REWARDED.value, TEST_AD_UNIT_ID)
        miniAppBridge.postMessage(jsonStr)
        verify(bridgeExecutor, times(2)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when cannot load interstitial`() {
        val errMsg = "${ErrorBridgeMessage.ERR_LOAD_AD} null"
        var jsonStr = createAdJsonStr(ActionType.LOAD_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        miniAppBridgeWithAdMob.postMessage(jsonStr)
        jsonStr = createAdJsonStr(ActionType.LOAD_AD.action, AdType.REWARDED.value, TEST_AD_UNIT_ID)
        miniAppBridgeWithAdMob.postMessage(jsonStr)

        verify(bridgeExecutor, times(2)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when cannot show interstitial`() {
        val errMsg = "${ErrorBridgeMessage.ERR_SHOW_AD} null"
        var jsonStr = createAdJsonStr(ActionType.SHOW_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        miniAppBridgeWithAdMob.postMessage(jsonStr)
        jsonStr = createAdJsonStr(ActionType.SHOW_AD.action, AdType.REWARDED.value, TEST_AD_UNIT_ID)
        miniAppBridgeWithAdMob.postMessage(jsonStr)

        verify(bridgeExecutor, times(2)).postError(TEST_CALLBACK_ID, errMsg)
    }
}

@RunWith(AndroidJUnit4::class)
class ScreenBridgeSpec : BridgeCommon() {
    private val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())

    @Before
    fun setupScreenBridgeDispatcher() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.allowScreenOrientation(false)
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
        }
    }

    private fun createCallbackJsonStr(orientation: ScreenOrientation) = Gson().toJson(
        CallbackObj(
            action = ActionType.SET_SCREEN_ORIENTATION.action,
            param = Screen(orientation.value),
            id = TEST_CALLBACK_ID
        )
    )

    @Suppress("LongMethod")
    @Test
    fun `postValue should be called when screen action is executed successfully`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(activity, webViewListener, mock(), mock(), TEST_MA_ID, mock(), mock())
            miniAppBridge.allowScreenOrientation(true)
            miniAppBridge.postMessage(createCallbackJsonStr(ScreenOrientation.LOCK_PORTRAIT))
            miniAppBridge.postMessage(createCallbackJsonStr(ScreenOrientation.LOCK_LANDSCAPE))
            miniAppBridge.postMessage(createCallbackJsonStr(ScreenOrientation.LOCK_RELEASE))
            miniAppBridge.onWebViewDetach()

            verify(bridgeExecutor, times(3)).postValue(TEST_CALLBACK_ID, SUCCESS)
        }
    }

    @Test
    fun `postValue should not be called when there is invalid action request`() {
        miniAppBridge.postMessage(Gson().toJson(
            CallbackObj(ActionType.SET_SCREEN_ORIENTATION.action, "", TEST_CALLBACK_ID))
        )

        verify(bridgeExecutor, times(0)).postValue(TEST_CALLBACK_ID, SUCCESS)
    }

    @Test
    fun `should not execute when hostapp does not allow miniapp to change screen orientation`() {
        val screenDispatcher = Mockito.spy(ScreenBridgeDispatcher(mock(), bridgeExecutor, false))
        screenDispatcher.onScreenRequest(mock())
        screenDispatcher.releaseLock()

        verify(bridgeExecutor, times(0)).postValue(TEST_CALLBACK_ID, SUCCESS)
    }
}
