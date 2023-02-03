package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.TestFilesDirActivity
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ShareContentBridgeSpec : BridgeCommon() {
    private val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())

    @Before
    fun setupShareInfo() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.setComponentsIAPDispatcher(mock())
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
            action = ActionType.SHARE_INFO.action, param = ShareInfoCallbackObj.ShareInfoParam(
                ShareInfo(content)
            ), id = TEST_CALLBACK_ID
        )
    )

    @Test
    fun `postValue should be called when content is shared successfully`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.setComponentsIAPDispatcher(mock())
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
            miniAppBridge.setComponentsIAPDispatcher(mock())
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

            verify(
                bridgeExecutor, times(0)
            ).dispatchEvent(NativeEventType.EXTERNAL_WEBVIEW_CLOSE.value, "")
        }
    }

    @Test
    fun `dispatchEvent should  be called if bridge executor is not initialized`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.setComponentsIAPDispatcher(mock())
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
            miniAppBridge.setComponentsIAPDispatcher(mock())
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
        miniAppBridge.setComponentsIAPDispatcher(mock())
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
