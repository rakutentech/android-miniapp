package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_AD_UNIT_ID
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.ads.TestAdMobDisplayer
import com.rakuten.tech.mobile.miniapp.closealert.MiniAppCloseAlertInfo
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

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
            action = action, param = AdObj(adType, adUnitId), id = TEST_CALLBACK_ID
        )
    )

    private fun initMiniAppBridge(adMobCheckedClass: String): MiniAppMessageBridge {
        AdMobClassName = adMobCheckedClass
        val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            miniAppBridge.updateApiClient(mock())
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
        miniAppBridge.setAdMobDisplayer(TestAdMobDisplayer())
        return miniAppBridge
    }

    @Test
    fun `postError should be called when AdMob is not provided`() {
        var errMsg =
            "${ErrorBridgeMessage.ERR_LOAD_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}"
        var jsonStr =
            createAdJsonStr(ActionType.LOAD_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        miniAppBridge.postMessage(jsonStr)
        jsonStr = createAdJsonStr(ActionType.LOAD_AD.action, AdType.REWARDED.value, TEST_AD_UNIT_ID)
        miniAppBridge.postMessage(jsonStr)
        verify(bridgeExecutor, times(2)).postError(TEST_CALLBACK_ID, errMsg)

        jsonStr =
            createAdJsonStr(ActionType.SHOW_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        errMsg = "${ErrorBridgeMessage.ERR_SHOW_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}"
        miniAppBridge.postMessage(jsonStr)
        jsonStr = createAdJsonStr(ActionType.SHOW_AD.action, AdType.REWARDED.value, TEST_AD_UNIT_ID)
        miniAppBridge.postMessage(jsonStr)
        verify(bridgeExecutor, times(2)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when cannot load interstitial`() {
        val errMsg = "${ErrorBridgeMessage.ERR_LOAD_AD} null"
        var jsonStr =
            createAdJsonStr(ActionType.LOAD_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
        miniAppBridgeWithAdMob.postMessage(jsonStr)
        jsonStr = createAdJsonStr(ActionType.LOAD_AD.action, AdType.REWARDED.value, TEST_AD_UNIT_ID)
        miniAppBridgeWithAdMob.postMessage(jsonStr)

        verify(bridgeExecutor, times(2)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when cannot show interstitial`() {
        val errMsg = "${ErrorBridgeMessage.ERR_SHOW_AD} null"
        var jsonStr =
            createAdJsonStr(ActionType.SHOW_AD.action, AdType.INTERSTITIAL.value, TEST_AD_UNIT_ID)
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
            miniAppBridge.updateApiClient(mock())
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
            miniAppBridge.updateApiClient(mock())
            miniAppBridge.init(
                activity, webViewListener, mock(), mock(), TEST_MA_ID, mock(), mock(), mock()
            )
            miniAppBridge.setInAppPurchaseProvider(mock())
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
        miniAppBridge.postMessage(
            Gson().toJson(
                CallbackObj(ActionType.SET_SCREEN_ORIENTATION.action, "", TEST_CALLBACK_ID)
            )
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

    @Test
    fun `miniAppShouldClose value should be assigned properly`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
            miniAppBridge.updateApiClient(mock())
            miniAppBridge.init(
                activity, webViewListener, mock(), mock(), TEST_MA_ID, mock(), mock(), mock()
            )
            val alertInfo = MiniAppCloseAlertInfo(true, "title", "desc")
            val closeAlertJsonStr = Gson().toJson(
                CallbackObj(
                    action = ActionType.SET_CLOSE_ALERT.action,
                    param = CloseAlertInfoCallbackObj.CloseAlertInfoParam(alertInfo),
                    id = TEST_CALLBACK_ID
                )
            )
            miniAppBridge.onMiniAppShouldClose(TEST_CALLBACK_ID, closeAlertJsonStr)
            miniAppBridge.miniAppShouldClose() shouldBeEqualTo alertInfo
        }
    }
}
