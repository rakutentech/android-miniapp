package com.rakuten.tech.mobile.miniapp.js.securestoragedispatcher

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.js.*
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
@Suppress("LargeClass")
class MessageBridgeSecurestorageDispatcherSpec : BridgeCommon() {
    private val unImplementedMessageBrdige = object : MiniAppMessageBridge() {}
    private val testKey = "key"
    private val testValue = "value"
    private val testItems: Map<String, String> = mapOf(testKey to testValue)
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

    private fun getCallbackObject(actionType: ActionType) = CallbackObj(
        id = TEST_CALLBACK_ID,
        action = actionType.action,
        param = testItems
    )

    private fun getCallbackObjToJson(callbackObj: CallbackObj): String =
        Gson().toJson(callbackObj)

    private fun getSecureStorageCallBackToJson(secureStorageCallbackObj: SecureStorageCallbackObj): String =
        Gson().toJson(secureStorageCallbackObj)

    private val miniappMessageBridge: MiniAppMessageBridge = Mockito.spy(
        createMiniAppMessageBridge(true)
    )

    private fun getMockSecureStorageDispatcher(): MiniAppSecureStorageDispatcher {
        val secureStorageDispatcher: MiniAppSecureStorageDispatcher = mock()
        When calling miniappMessageBridge.miniAppSecureStorageDispatcher itReturns secureStorageDispatcher
        return secureStorageDispatcher
    }

    @Before
    fun setupShareInfo() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            miniAppSecureStorageDispatcher =
                spy(MiniAppSecureStorageDispatcher(activity, testMaxStorageSizeInKB))
            miniAppSecureStorageDispatcher.bridgeExecutor = bridgeExecutor
            miniAppSecureStorageDispatcher.setMiniAppComponents(TEST_MA_ID)
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

    @Test(expected = MiniAppSdkException::class)
    fun `getMessaginguniqueId should throw MiniAppSdkException when it is not implemented`() {
        unImplementedMessageBrdige.getMessagingUniqueId(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `getMauid should throw MiniAppSdkException when it is not implemented`() {
        unImplementedMessageBrdige.getMauid(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `requestDevicepPermission throw MiniAppSdkException when it is not implemented`() {
        unImplementedMessageBrdige.requestDevicePermission(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `requestCustomPermissions throw MiniAppSdkException when it is not implemented`() {
        unImplementedMessageBrdige.requestCustomPermissions(mock(), mock())
    }

    @Test
    fun `securestorageDispatcher should call onGetItem when secureStorageCallbackObj is valid`() {
        val secureStorageDispatcher = getMockSecureStorageDispatcher()
        val callbackJson =
            getCallbackObjToJson(getCallbackObject(ActionType.SECURE_STORAGE_GET_ITEM))
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
            getCallbackObjToJson(getCallbackObject(ActionType.SECURE_STORAGE_REMOVE_ITEMS))
        miniappMessageBridge.postMessage(callbackJson)
        verify(secureStorageDispatcher).onRemoveItems(
            secureStorageCallbackObj.id,
            callbackJson
        )
    }

    @Test
    fun `securestorageDispatcher should call onClearAll when secureStorageCallbackObj is valid`() {
        val secureStorageDispatcher = getMockSecureStorageDispatcher()
        val callbackJson = getCallbackObjToJson(getCallbackObject(ActionType.SECURE_STORAGE_CLEAR))
        miniappMessageBridge.postMessage(callbackJson)
        verify(secureStorageDispatcher).onClearAll(
            secureStorageCallbackObj.id
        )
    }

    @Test
    fun `securestorageDispatcher should call onSetItems when secureStorageCallbackObj is valid`() {
        val secureStorageDispatcher = getMockSecureStorageDispatcher()
        val callbackJson = getSecureStorageCallBackToJson(secureStorageCallbackObj)
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
        val callbackJson = getSecureStorageCallBackToJson(secureStorageCallbackObj)
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
        val callbackJson = getCallbackObjToJson(closeAlertCallbackObj)
        miniappMessageBridge.postMessage(callbackJson)
        verify(miniappMessageBridge).onMiniAppShouldClose(closeAlertCallbackObj.id, callbackJson)
    }
}
