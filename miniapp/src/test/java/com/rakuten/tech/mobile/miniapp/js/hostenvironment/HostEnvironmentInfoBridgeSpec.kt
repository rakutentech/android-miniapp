package com.rakuten.tech.mobile.miniapp.js.hostenvironment

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.errors.MiniAppBridgeErrorModel
import com.rakuten.tech.mobile.miniapp.js.*
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class HostEnvironmentInfoBridgeSpec {
    private lateinit var miniAppBridge: MiniAppMessageBridge
    private val hostEnvCallbackObj = CallbackObj(
            action = ActionType.GET_HOST_ENVIRONMENT_INFO.action,
            param = null,
            id = TEST_CALLBACK_ID
    )

    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))

    private val hostEnvironmentInfo = HostEnvironmentInfo("Android 30", "012345", "X.X.X")
    private val infoErrMessage = "{\"type\":\"${HostEnvironmentInfoBridge.ERR_GET_ENVIRONMENT_INFO} $TEST_ERROR_MSG\"}"
    private val testEnvironmentError = HostEnvironmentInfoError(null, infoErrMessage)
    private fun createEnvironmentImpl(canGetInfo: Boolean, isImplemented: Boolean): HostEnvironmentBridgeDispatcher {
        return if (isImplemented) {
            object : HostEnvironmentBridgeDispatcher {
                override fun getHostEnvironmentInfo(
                    onSuccess: (info: HostEnvironmentInfo) -> Unit,
                    onError: (infoError: HostEnvironmentInfoError) -> Unit
                ) {
                    if (canGetInfo)
                        onSuccess.invoke(hostEnvironmentInfo)
                    else
                        onError.invoke(testEnvironmentError)
                }
            }
        } else {
            object : HostEnvironmentBridgeDispatcher {}
        }
    }

    @Before
    fun setup() {
        miniAppBridge = Mockito.spy(object : MiniAppMessageBridge() {})
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
                activity = TestActivity(),
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA.id,
                ratDispatcher = mock()
        )
    }

    @Test
    @SuppressWarnings("MaximumLineLength")
    fun `postError should be called when there is no get environment info retrieval implementation`() {
        val dispatcher = Mockito.spy(createEnvironmentImpl(false, false))
        miniAppBridge.setHostEnvironmentBridgeDispatcher(dispatcher)
        val errMsg = "{\"type\":\"${HostEnvironmentInfoBridge.ERR_GET_ENVIRONMENT_INFO}" +
                " ${ErrorBridgeMessage.NO_IMPL}\"}"
        miniAppBridge.postMessage(Gson().toJson(hostEnvCallbackObj))

        verify(bridgeExecutor).postError(hostEnvCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp doesn't providing environment info`() {
        val dispatcher = Mockito.spy(createEnvironmentImpl(false, true))
        val userInfoBridgeWrapper = Mockito.spy(createBridgeWrapper(dispatcher))
        userInfoBridgeWrapper.onGetHostEnvironmentInfo(hostEnvCallbackObj.id)

        verify(bridgeExecutor).postError(hostEnvCallbackObj.id, Gson().toJson(
                MiniAppBridgeErrorModel(message = infoErrMessage)
        ))
    }

    @Test
    fun `postValue should be called when retrieve environment info successfully`() {
        val dispatcher = Mockito.spy(createEnvironmentImpl(true, true))
        val userInfoBridgeWrapper = Mockito.spy(createBridgeWrapper(dispatcher))
        userInfoBridgeWrapper.onGetHostEnvironmentInfo(hostEnvCallbackObj.id)

        verify(bridgeExecutor).postValue(hostEnvCallbackObj.id, Gson().toJson(hostEnvironmentInfo))
    }

    private fun createBridgeWrapper(dispatcher: HostEnvironmentBridgeDispatcher): HostEnvironmentInfoBridge {
        val wrapper = HostEnvironmentInfoBridge()
        wrapper.setMiniAppComponent(bridgeExecutor)
        wrapper.setHostEnvironmentBridgeDispatcher(dispatcher)
        return wrapper
    }
}
