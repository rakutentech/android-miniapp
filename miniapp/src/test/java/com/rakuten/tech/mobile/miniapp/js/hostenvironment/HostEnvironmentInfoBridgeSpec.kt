package com.rakuten.tech.mobile.miniapp.js.hostenvironment

import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.js.*
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class HostEnvironmentInfoBridgeSpec : BridgeCommon() {
    private val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())

    @Before
    fun setup() {
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
                secureStorageDispatcher = mock(),
                miniAppIAPVerifier = mock()
            )
        }
    }

    private val hostEnvCallbackObj = CallbackObj(
            action = ActionType.GET_HOST_ENVIRONMENT_INFO.action,
            param = null,
            id = TEST_CALLBACK_ID
    )
    private val hostEnvJsonStr = Gson().toJson(hostEnvCallbackObj)

    @Test
    fun `postValue should be called when retrieve environment info successfully`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val hostEnvironmentInfo = HostEnvironmentInfo(
                    platformVersion = Build.VERSION.RELEASE,
                    hostVersion = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName,
                    sdkVersion = BuildConfig.VERSION_NAME,
                    hostLocale = Locale.getDefault().toLanguageTag()
            )
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
                    secureStorageDispatcher = mock(),
                    miniAppIAPVerifier = mock()
            )
            miniAppBridge.postMessage(hostEnvJsonStr)
            verify(bridgeExecutor).postValue(hostEnvCallbackObj.id, Gson().toJson(hostEnvironmentInfo))
        }
    }

    @Test
    fun `postError should be called when cannot retrieve environment info`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(false))
            val infoErrMessage =
                "{\"type\":\"{\\\"type\\\":\\\"Cannot get host environment info: error_message\\\"}\"}"
            val webViewListener = createErrorWebViewListener(infoErrMessage)
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
                secureStorageDispatcher = mock(),
                miniAppIAPVerifier = mock()
            )
            miniAppBridge.postMessage(hostEnvJsonStr)

            verify(bridgeExecutor).postError(TEST_CALLBACK_ID, infoErrMessage)
        }
    }

    @Test
    fun `postValue should not be called when using default method without Activity`() {
        miniAppBridge.postMessage(hostEnvJsonStr)

        verify(bridgeExecutor, times(0)).postValue(TEST_CALLBACK_ID, "anyValue")
    }
}
