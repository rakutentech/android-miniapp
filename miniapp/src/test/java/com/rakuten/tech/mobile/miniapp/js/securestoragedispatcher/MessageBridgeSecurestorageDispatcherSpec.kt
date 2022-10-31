package com.rakuten.tech.mobile.miniapp.js.securestoragedispatcher

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MAX_STORAGE_SIZE_IN_BYTES
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_STORAGE_VERSION
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.js.*
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
@Suppress("LargeClass")
class MessageBridgeSecurestorageDispatcherSpec : BridgeCommon() {

    private var context: Context = mock()

    private lateinit var miniAppSecureStorage: MiniAppSecureStorage
    private lateinit var miniAppSecureStorageDispatcher: MiniAppSecureStorageDispatcher
    private val testMaxStorageSizeInKB = 1000L

    private val secureStorageCallbackObj = SecureStorageCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageItems(testItems),
        id = TEST_CALLBACK_ID
    )

    private val secureStorageCallbackFailObj = CallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = testItems,
        id = TEST_CALLBACK_ID
    )

    private val miniappMessageBridge: MiniAppMessageBridge = Mockito.spy(
        createMiniAppMessageBridge(true)
    )

    private fun getMockSecureStorageDispatcher(): MiniAppSecureStorageDispatcher {
        val secureStorageDispatcher: MiniAppSecureStorageDispatcher = mock()
        When calling miniappMessageBridge.miniAppSecureStorageDispatcher itReturns secureStorageDispatcher
        return secureStorageDispatcher
    }

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            miniAppSecureStorageDispatcher =
                spy(MiniAppSecureStorageDispatcher(activity, TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()))
            miniAppSecureStorage = Mockito.spy(MiniAppSecureStorage(
                context, TEST_STORAGE_VERSION, TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()
            ))
            miniAppSecureStorageDispatcher.bridgeExecutor = bridgeExecutor
            miniAppSecureStorageDispatcher.setMiniAppComponents(TEST_MA_ID)
            miniAppSecureStorageDispatcher.onFailed = mock()
            miniAppSecureStorageDispatcher.onSuccess = mock()

            When calling miniappMessageBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniappMessageBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            miniappMessageBridge.setMiniAppFileDownloader(mock())
            miniappMessageBridge.setChatBridgeDispatcher(mock())
            miniappMessageBridge.miniAppSecureStorageDispatcher = miniAppSecureStorageDispatcher
        }
    }

    @Test
    fun `securestorageDispatcher should call onLoad when onJsinjectionisDone`() {
        val securestoragedispatcher: MiniAppSecureStorageDispatcher = mock()
        When calling miniappMessageBridge.miniAppSecureStorageDispatcher itReturns securestoragedispatcher
        miniappMessageBridge.onJsInjectionDone()
        verify(securestoragedispatcher).onLoad()
    }

    @Test
    fun `securestorageDispatcher should call onGetItem when secureStorageCallbackObj is valid`() {
        val secureStorageDispatcher = getMockSecureStorageDispatcher()
        val callbackJson =
            getCallbackObjToJsonStr(getCallbackObject(ActionType.SECURE_STORAGE_GET_ITEM))
        miniappMessageBridge.postMessage(callbackJson)
        verify(secureStorageDispatcher).onGetItem(
            secureStorageCallbackObj.id,
            callbackJson
        )
    }

    @Test
    fun `securestorageDispatcher should call onRemoveitems when secureStorageCallbackObj is valid`() {
        val secureStorageDispatcher = getMockSecureStorageDispatcher()
        val callbackJson =
            getCallbackObjToJsonStr(getCallbackObject(ActionType.SECURE_STORAGE_REMOVE_ITEMS))
        miniappMessageBridge.postMessage(callbackJson)
        verify(secureStorageDispatcher).onRemoveItems(
            secureStorageCallbackObj.id,
            callbackJson
        )
    }

    @Test
    fun `securestorageDispatcher should call onClearAll when secureStorageCallbackObj is valid`() {
        val secureStorageDispatcher = getMockSecureStorageDispatcher()
        val callbackJson = getCallbackObjToJsonStr(getCallbackObject(ActionType.SECURE_STORAGE_CLEAR))
        miniappMessageBridge.postMessage(callbackJson)
        verify(secureStorageDispatcher).onClearAll(
            secureStorageCallbackObj.id
        )
    }

    @Test
    fun `securestorageDispatcher should call onSize when secureStorageCallbackObj is valid`() {
        val secureStorageDispatcher = getMockSecureStorageDispatcher()
        val callbackJson = getCallbackObjToJsonStr(getCallbackObject(ActionType.SECURE_STORAGE_SIZE))
        miniappMessageBridge.postMessage(callbackJson)
        verify(secureStorageDispatcher).onSize(
            secureStorageCallbackObj.id
        )
    }

    @Test
    fun `securestorageDispatcher should call onSetItems when secureStorageCallbackObj is valid`() {
        val secureStorageDispatcher = getMockSecureStorageDispatcher()
        val callbackJson = getSecureStorageCallBackToJsonStr(secureStorageCallbackObj)
        miniappMessageBridge.postMessage(callbackJson)
        verify(secureStorageDispatcher).onSetItems(
            secureStorageCallbackObj.id,
            callbackJson
        )
    }

    @Test
    fun `secureStorage should call insertItems when secureStorageCallbackObj is valid`() {
        val secureStorage: MiniAppSecureStorage = mock()
        When calling miniappMessageBridge.miniAppSecureStorageDispatcher.miniAppSecureStorage itReturns secureStorage
        val callbackJson = getSecureStorageCallBackToJsonStr(secureStorageCallbackObj)
        miniappMessageBridge.postMessage(callbackJson)
        verify(secureStorage).insertItems(
            secureStorageCallbackObj.param.secureStorageItems!!,
            miniAppSecureStorageDispatcher.onSuccess,
            miniAppSecureStorageDispatcher.onFailed
        )
    }

    @Test
    fun `bridgeExecutor should call postError when secureStorageCallbackObj is not valid`() {
        val bridgeExecutor: MiniAppBridgeExecutor = mock()
        When calling miniappMessageBridge.miniAppSecureStorageDispatcher.bridgeExecutor itReturns bridgeExecutor
        miniappMessageBridge.postMessage(Gson().toJson(secureStorageCallbackFailObj))
        verify(bridgeExecutor).postError(
            secureStorageCallbackFailObj.id,
            MiniAppSecureStorageDispatcher.ERR_WRONG_JSON_FORMAT
        )
    }

    @Test
    fun `should call onMiniappshouldclose when postMessage with SET_CLOSE_ALERT actionType is called`() {
        val closeAlertCallbackObj = getCallbackObject(ActionType.SET_CLOSE_ALERT)
        val callbackJson = getCallbackObjToJsonStr(closeAlertCallbackObj)
        miniappMessageBridge.postMessage(callbackJson)
        verify(miniappMessageBridge).onMiniAppShouldClose(closeAlertCallbackObj.id, callbackJson)
    }

    @Test
    fun `bridgeExecutor should call postError when CloseAlertInfoCallbackObj is not valid`() {
        val closeAlertCallbackObj = getCallbackObject(ActionType.SET_CLOSE_ALERT, params = null)
        val callbackJson = getCallbackObjToJsonStr(closeAlertCallbackObj)
        miniappMessageBridge.postMessage(callbackJson)
        verify(bridgeExecutor).postError(
            closeAlertCallbackObj.id,
            ErrorBridgeMessage.ERR_CLOSE_ALERT
        )
    }

    @Test
    fun `fundispatchNativeEvent should be called when bridgeExecutor is initialized`() {
        val testNativeEventType = NativeEventType.MINIAPP_ON_PAUSE
        miniappMessageBridge.dispatchNativeEvent(testNativeEventType)
        verify(bridgeExecutor).dispatchEvent(
            testNativeEventType.value, ""
        )
    }
}
