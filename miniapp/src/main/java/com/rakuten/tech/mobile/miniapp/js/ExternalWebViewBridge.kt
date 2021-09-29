package com.rakuten.tech.mobile.miniapp.js

import com.google.gson.Gson
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
            val successCallback = { info: String ->
                bridgeExecutor.postValue(callbackId, Gson().toJson(info))
            }
            val errorCallback = { callback: String ->
                bridgeExecutor.postError(callbackId, Gson().toJson(ERR_GET_ENVIRONMENT_INFO))
            }

            dispatcher.onExternalWebViewClose(successCallback, errorCallback)
        } catch (e: Exception) {
            bridgeExecutor.postError(
                callbackId,
                ERR_GET_ENVIRONMENT_INFO
            )
        }
    }

    internal companion object {
        const val ERR_GET_ENVIRONMENT_INFO = "Cannot get host environment info:"
    }
}
