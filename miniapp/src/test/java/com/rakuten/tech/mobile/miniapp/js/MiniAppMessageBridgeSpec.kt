package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.js.hostenvironment.HostEnvironmentInfo
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@Suppress("LongMethod", "LargeClass")
class MiniAppMessageBridgeSpec : BridgeCommon() {
    override var mockIsValid = true
    private val miniAppBridge: MiniAppMessageBridge = Mockito.spy(
        createMiniAppMessageBridge(false)
    )

    private val messagingUniqueIdCallbackObj = CallbackObj(
        action = ActionType.GET_MESSAGING_UNIQUE_ID.action, param = null, id = TEST_CALLBACK_ID
    )
    private val messagingUniqueIdJsonStr = Gson().toJson(messagingUniqueIdCallbackObj)

    private val mauIdCallbackObj = CallbackObj(
        action = ActionType.GET_MAUID.action, param = null, id = TEST_CALLBACK_ID
    )
    private val mauIdJsonStr = Gson().toJson(mauIdCallbackObj)

    private val permissionCallbackObj = CallbackObj(
        action = ActionType.REQUEST_PERMISSION.action,
        param = Gson().toJson(DevicePermission(MiniAppDevicePermissionType.LOCATION.type)),
        id = TEST_CALLBACK_ID
    )
    private val permissionJsonStr = Gson().toJson(permissionCallbackObj)

    private val hostEnvInfoCallbackObj = CallbackObj(
        action = ActionType.GET_HOST_ENVIRONMENT_INFO.action, param = null, id = TEST_CALLBACK_ID
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
    fun `onGetUniqueId should call getUniqueId`() {
        miniAppBridge.onGetUniqueId(getCallbackObject(ActionType.GET_UNIQUE_ID))
        verify(miniAppBridge).getUniqueId(any(), any())
    }

    @Test
    fun `onGetMessagingUniqueId should invoke errorCallback when it is not valid`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val errMsg = "${ErrorBridgeMessage.ERR_MESSAGING_UNIQUE_ID} $testErrorMessage"
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            mockIsValid = false
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

    @Test
    fun `onGetMauId should invoke errorCallback when it is not valid`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val errMsg = "${ErrorBridgeMessage.ERR_MAUID} $testErrorMessage"
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            mockIsValid = false
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

            verify(bridgeExecutor).postValue(
                permissionCallbackObj.id,
                MiniAppDevicePermissionResult.getValue(isPermissionGranted).type
            )
        }
    }

    @Test
    fun `onRequesDevicePermissionResult should be called when device permission is granted`() {
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

            verify(miniAppBridge).onRequestDevicePermissionsResult(
                permissionCallbackObj.id, true
            )
        }
    }

    @Test
    fun `postError should be called when device permission is denied`() {
        miniAppBridge.postMessage(permissionJsonStr)
        verify(bridgeExecutor).postError(
            permissionCallbackObj.id, MiniAppDevicePermissionResult.getValue(false).type
        )
    }

    @Test
    fun `postError should be called when requestDevicePermission throws an Exception`() {
        mockIsException = true
        miniAppBridge.postMessage(permissionJsonStr)
        verify(bridgeExecutor).postError(
            permissionCallbackObj.id,
            "${ErrorBridgeMessage.ERR_REQ_DEVICE_PERMISSION} $testErrorMessage"
        )
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
        val errMsg =
            "{\"type\":\"{\\\"type\\\":\\\"Cannot get host environment info: error_message\\\"}\"}"
        miniAppBridge.onGetHostEnvironmentInfo(hostEnvInfoCallbackObj.id)
        verify(bridgeExecutor).postError(hostEnvInfoCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when success callback invoked to get host environment info`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(false, true))
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
            val info = HostEnvironmentInfo(activity, "en")
            miniAppBridge.onGetHostEnvironmentInfo(hostEnvInfoCallbackObj.id)
            verify(bridgeExecutor).postValue(hostEnvInfoCallbackObj.id, Gson().toJson(info))
        }
    }

    /** end region */

    @Test
    fun `close listener should be invoked with correct param`() {
        var closeMiniAppCallbackObj = CallbackObj(
            action = ActionType.CLOSE_MINIAPP.action,
            param = Gson().toJson(CloseMiniAppCallbackObj(withConfirmationAlert = true)),
            id = TEST_CALLBACK_ID
        )
        var miniAppCloseJsonStr = Gson().toJson(closeMiniAppCallbackObj)

        val callback: (Boolean) -> Unit = mock()
        miniAppBridge.setMiniAppCloseListener(callback)
        miniAppBridge.postMessage(miniAppCloseJsonStr)
        verify(callback).invoke(true)

        closeMiniAppCallbackObj = CallbackObj(
            action = ActionType.CLOSE_MINIAPP.action,
            param = Gson().toJson(CloseMiniAppCallbackObj(withConfirmationAlert = false)),
            id = TEST_CALLBACK_ID
        )
        miniAppCloseJsonStr = Gson().toJson(closeMiniAppCallbackObj)

        miniAppBridge.postMessage(miniAppCloseJsonStr)
        verify(callback).invoke(false)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `close listener should not be invoked when param is null`() {
        val closeMiniAppCallbackObj = CallbackObj(
            action = ActionType.CLOSE_MINIAPP.action,
            param = null,
            id = TEST_CALLBACK_ID
        )
        val miniAppCloseJsonStr = Gson().toJson(closeMiniAppCallbackObj)
        val callback: (Boolean) -> Unit = mock()
        miniAppBridge.setMiniAppCloseListener(callback)
        miniAppBridge.postMessage(miniAppCloseJsonStr)
        verify(callback, never()).invoke(any())

        miniAppBridge.postMessage(miniAppCloseJsonStr)
        verify(miniAppBridge.miniAppCloseListener, never()).invoke(any())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `close listener should not be invoked if miniAppCloseListener is not initialized`() {
        val closeMiniAppCallbackObj = CallbackObj(
            action = ActionType.CLOSE_MINIAPP.action,
            param = Gson().toJson(CloseMiniAppCallbackObj(withConfirmationAlert = false)),
            id = TEST_CALLBACK_ID
        )
        val miniAppCloseJsonStr = Gson().toJson(closeMiniAppCallbackObj)
        miniAppBridge.postMessage(miniAppCloseJsonStr)
    }

    @Test
    fun `all error bridge messages should be expected`() {
        assertEquals("no implementation by the Host App.", ErrorBridgeMessage.NO_IMPL)
        assertEquals("No support from hostapp", ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP)
        assertEquals("Cannot get unique id:", ErrorBridgeMessage.ERR_UNIQUE_ID)
        assertEquals(
            "Cannot request device permission:", ErrorBridgeMessage.ERR_REQ_DEVICE_PERMISSION
        )
        assertEquals(
            "Cannot request custom permissions:", ErrorBridgeMessage.ERR_REQ_CUSTOM_PERMISSION
        )
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
        assertEquals(
            "Cannot get host environment info:", ErrorBridgeMessage.ERR_GET_ENVIRONMENT_INFO
        )
    }
}
