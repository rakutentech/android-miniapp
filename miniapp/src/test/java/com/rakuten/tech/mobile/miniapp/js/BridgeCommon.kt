package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ActivityScenario
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.ERR_GET_ENVIRONMENT_INFO
import com.rakuten.tech.mobile.miniapp.js.hostenvironment.HostEnvironmentInfo
import com.rakuten.tech.mobile.miniapp.js.hostenvironment.HostEnvironmentInfoError
import com.rakuten.tech.mobile.miniapp.permission.*
import org.junit.Assert
import org.mockito.Mockito
import org.mockito.kotlin.mock

@Suppress("TooGenericExceptionThrown")
open class BridgeCommon {
    internal val webViewListener: WebViewListener = mock()
    internal val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
    open var mockIsValid = true
    open val testErrorMessage = "test error message"
    open var mockIsException = false
    internal val testKey = "key"
    internal val testValue = "value"
    internal val testItems: Map<String, String> = mapOf(testKey to testValue)

    internal fun getCallbackObjToJson(callbackObj: CallbackObj): String =
        Gson().toJson(callbackObj)

    internal fun getSecureStorageCallBackToJson(secureStorageCallbackObj: SecureStorageCallbackObj): String =
        Gson().toJson(secureStorageCallbackObj)

    internal fun getCallbackObject(actionType: ActionType, params: Map<String, String>? = testItems) = CallbackObj(
        id = TEST_CALLBACK_ID,
        action = actionType.action,
        param = params
    )

    fun createMiniAppMessageBridge(
        isPermissionGranted: Boolean,
        hasEnvInfo: Boolean = false
    ): MiniAppMessageBridge = object : MiniAppMessageBridge() {

        override fun getUniqueId(
            onSuccess: (uniqueId: String) -> Unit,
            onError: (message: String) -> Unit
        ) {
            onSuccess(TEST_CALLBACK_VALUE)
        }

        override fun requestCustomPermissions(
            permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
            callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
        ) {
            if (mockIsException) {
                throw CustomPermissionsNotImplementedException()
            }
            super.requestCustomPermissions(permissionsWithDescription, callback)
        }

        override fun getMessagingUniqueId(
            onSuccess: (uniqueId: String) -> Unit,
            onError: (message: String) -> Unit
        ) {
            if (mockIsValid) {
                onSuccess(TEST_CALLBACK_VALUE)
            } else {
                onError(testErrorMessage)
            }
        }

        override fun getMauid(
            onSuccess: (mauId: String) -> Unit,
            onError: (message: String) -> Unit
        ) {
            if (mockIsValid) {
                onSuccess(TEST_CALLBACK_VALUE)
            } else {
                onError(testErrorMessage)
            }
        }

        override fun requestDevicePermission(
            miniAppPermissionType: MiniAppDevicePermissionType,
            callback: (isGranted: Boolean) -> Unit
        ) {
            if (mockIsException) {
                throw MiniAppSdkException(testErrorMessage)
            }
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

    protected fun createDefaultMiniAppMessageBridge(): MiniAppMessageBridge =
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
