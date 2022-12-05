package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.permission.*
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.itThrows
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
@Suppress("LargeClass")
class UniversalBridgeSpec : BridgeCommon() {

    private val miniAppBridge: MiniAppMessageBridge = Mockito.spy(
        createDefaultMiniAppMessageBridge()
    )

    private fun createJsonInfoCallbackJsonStr() = Gson().toJson(
        JsonInfoCallbackObj.JsonInfoParam(
            JsonInfo("content")
        )
    )

    private fun createJsonInfoCallbackJsonStr(content: String) = Gson().toJson(
        CallbackObj(
            action = ActionType.JSON_INFO.action, param = JsonInfoCallbackObj.JsonInfoParam(
                JsonInfo(content)
            ), id = TEST_CALLBACK_ID
        )
    )

    private val jsonInfoJsonStr = createJsonInfoCallbackJsonStr(createJsonInfoCallbackJsonStr())

    @Before
    fun setUp() {
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
    fun `miniAppBridge should call onSendJsonToHostApp if universal bridge json is valid`() {
        miniAppBridge.postMessage(jsonInfoJsonStr)
        verify(miniAppBridge).onSendJsonToHostApp(
            TEST_CALLBACK_ID, jsonInfoJsonStr
        )
    }

    @Test
    fun `miniAppBridge should call sendJsonToHostApp if universal bridge json is valid`() {
        miniAppBridge.postMessage(jsonInfoJsonStr)
        verify(miniAppBridge).sendJsonToHostApp(
            eq(createJsonInfoCallbackJsonStr()),
            any(),
            any()
        )
    }

    @Test
    fun `should invoke onError if universal bridge content is empty`() {
        val onError: (String) -> Unit = Mockito.spy { }
        val errorMessage = ErrorBridgeMessage.ERR_JSON_INFO
        miniAppBridge.sendJsonToHostApp("", onSuccess = {}, onError = onError)
        verify(onError).invoke(errorMessage)
    }

    @Test
    fun `should invoke postError if universal bridge content is not valid`() {
        miniAppBridge.postMessage(createJsonInfoCallbackJsonStr("error"))
        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, ErrorBridgeMessage.ERR_JSON_INFO)
    }

    @Test
    fun `should invoke onError if universal bridge content is not valid`() {
        val onError: (String) -> Unit = Mockito.spy { }
        miniAppBridge.sendJsonToHostApp("error", onSuccess = {}, onError = onError)
        verify(onError).invoke(ErrorBridgeMessage.ERR_JSON_INFO)
    }

    @Test
    fun `should invoke onSuccess if universal bridge json is valid`() {
        val onSuccess: (Any) -> Unit = Mockito.spy { }
        val content = "{\"content\": \"test\"}"
        miniAppBridge.sendJsonToHostApp(content, onSuccess = onSuccess, onError = { })
        verify(onSuccess).invoke(content)
    }

    @Test
    fun `bridgeExecutor should call postValue if universal bridge json is valid`() {
        miniAppBridge.postMessage(jsonInfoJsonStr)
        verify(bridgeExecutor, times(1)).postValue(
            TEST_CALLBACK_ID, createJsonInfoCallbackJsonStr(),
        )
    }

    @Test
    fun `webViewListener should call runSuccessCallback if universal bridge json is valid`() {
        miniAppBridge.postMessage(jsonInfoJsonStr)
        verify(webViewListener).runSuccessCallback(
            TEST_CALLBACK_ID, createJsonInfoCallbackJsonStr(),
        )
    }

    @Test(expected = RuntimeException::class)
    fun `bridgeExecutor postValue should throw an exception`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            val webViewListener =
                spy(createErrorWebViewListener(ErrorBridgeMessage.ERR_JSON_INFO))
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            When calling webViewListener.runSuccessCallback(
                any(),
                any()
            ) itThrows RuntimeException()
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )

            miniAppBridge.postMessage(jsonInfoJsonStr)
            given(
                bridgeExecutor.postValue(
                    TEST_CALLBACK_ID, any()
                )
            ).willThrow(RuntimeException())
        }
    }

    @Test(expected = RuntimeException::class)
    fun `bridgeExecutor postError should throw an exception`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            When calling webViewListener.runErrorCallback(
                org.amshove.kluent.any(),
                org.amshove.kluent.any()
            ) itThrows RuntimeException()
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )

            val nullUniversalBridge = CallbackObj(
                action = ActionType.JSON_INFO.action, param = null, id = TEST_CALLBACK_ID
            )

            miniAppBridge.postMessage(Gson().toJson(nullUniversalBridge))
            given(
                bridgeExecutor.postError(
                    TEST_CALLBACK_ID, any()
                )
            ).willThrow(RuntimeException())
        }
    }

    @Test
    fun `bridgeExecutor should call postValue if universalBridge json is valid`() {
        miniAppBridge.postMessage(jsonInfoJsonStr)
        verify(bridgeExecutor).postValue(
            TEST_CALLBACK_ID, createJsonInfoCallbackJsonStr(),
        )
    }

    @Test
    fun `bridgeExecutor should call postError if universalBridge json is null`() {
        val nullUniversalBridge = CallbackObj(
            action = ActionType.JSON_INFO.action, param = null, id = TEST_CALLBACK_ID
        )

        miniAppBridge.postMessage(Gson().toJson(nullUniversalBridge))
        verify(bridgeExecutor).postError(eq(TEST_CALLBACK_ID), any())
    }

    @Test
    fun `webViewListener should call runErrorCallback if universalBridge json is not valid`() {

        val nullUniversalBridge = CallbackObj(
            action = ActionType.JSON_INFO.action, param = null, id = TEST_CALLBACK_ID
        )

        miniAppBridge.postMessage(Gson().toJson(nullUniversalBridge))
        verify(webViewListener).runErrorCallback(eq(TEST_CALLBACK_ID), any())
    }

    @Test
    fun `bridgeExecutor should call postError if universalBridge json is not valid`() {

        val nullUniversalBridge = CallbackObj(
            action = ActionType.JSON_INFO.action, param = "", id = TEST_CALLBACK_ID
        )

        miniAppBridge.postMessage(Gson().toJson(nullUniversalBridge))
        verify(bridgeExecutor).postError(eq(TEST_CALLBACK_ID), any())
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun `miniAppBridge post message should throw an exception if bridge executor is not initialized`() {
        val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
        miniAppBridge.postMessage(jsonInfoJsonStr)
    }

    @Test
    fun `bridgeExecutor postValue should not be called if it is not initialized`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
            miniAppBridge.postMessage(jsonInfoJsonStr)

            verify(
                bridgeExecutor, times(0)
            ).postValue(TEST_CALLBACK_ID, "")
        }
    }

    @Test
    fun `native event type should return the correct value`() {
        NativeEventType.MINIAPP_RECEIVE_JSON_INFO.value shouldBeEqualTo "miniappreceivejsoninfo"
    }
}
