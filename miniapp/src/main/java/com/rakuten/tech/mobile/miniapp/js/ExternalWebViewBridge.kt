package com.rakuten.tech.mobile.miniapp.js

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.errors.MiniAppBridgeErrorModel
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL

internal class ExternalWebViewBridge {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private var isMiniAppComponentReady = false
    private lateinit var dispatcher: ExternalWebviewDispatcher

    fun setMiniAppComponent(bridgeExecutor: MiniAppBridgeExecutor) {
        this.bridgeExecutor = bridgeExecutor
        isMiniAppComponentReady = true
    }

    @SuppressWarnings("FunctionMaxLength")
    fun setExternalWebviewDispatcher(dispatcher: ExternalWebviewDispatcher) {
        this.dispatcher = dispatcher
    }

    private fun <T> whenReady(callbackId: String, callback: () -> T) {
        if (isMiniAppComponentReady) {
            if (this::dispatcher.isInitialized)
                callback.invoke()
            else
                bridgeExecutor.postError(callbackId, NO_IMPL)
        }
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    fun onExternalWebViewClose(callbackId: String) = whenReady(callbackId) {
        try {
            val successCallback = { message: String ->
                bridgeExecutor.postValue(callbackId, Gson().toJson(message))
            }
            val errorCallback = { errorMsg: String ->
                val errorBridgeModel = MiniAppBridgeErrorModel(message = errorMsg)
                bridgeExecutor.postError(callbackId, Gson().toJson(errorBridgeModel))
            }

            dispatcher.onExternalWebViewClose(successCallback, errorCallback)
        } catch (e: Exception) {
            bridgeExecutor.postError(
                callbackId, Gson().toJson(
                    MiniAppBridgeErrorModel(
                        "$ERR_EXTERNAL_WEBVIEW_CLOSE ${e.message}"
                    )
                )
            )
        }
    }

    internal companion object {
        const val ERR_EXTERNAL_WEBVIEW_CLOSE = "Error closing on external webview:"
    }
}
