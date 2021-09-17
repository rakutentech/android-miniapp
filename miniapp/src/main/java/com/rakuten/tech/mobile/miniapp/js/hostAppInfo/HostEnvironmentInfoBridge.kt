package com.rakuten.tech.mobile.miniapp.js.hostAppInfo

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.errors.MiniAppBridgeErrorModel
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor

internal class HostEnvironmentInfoBridge {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private var isMiniAppComponentReady = false
    private lateinit var dispatcher: HostEnvironmentBridgeDispatcher

    fun setMiniAppComponents(bridgeExecutor: MiniAppBridgeExecutor) {
        this.bridgeExecutor = bridgeExecutor
        isMiniAppComponentReady = true
    }

    fun setHostEnvironmentBridgeDispatcher(dispatcher: HostEnvironmentBridgeDispatcher) {
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

    fun onGetHostEnvironmentInfo(callbackId: String) = whenReady(callbackId) {
        try {
            val successCallback = { info: HostEnvironmentInfo ->
                bridgeExecutor.postValue(callbackId, Gson().toJson(info))
            }
            val errorCallback = { callback: HostEnvironmentInfoError ->
                val errorBridgeModel = MiniAppBridgeErrorModel(callback.type, callback.message)
                bridgeExecutor.postError(callbackId, Gson().toJson(errorBridgeModel))
            }

            dispatcher.getHostEnvironmentInfo(successCallback, errorCallback)
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId,
                    Gson().toJson(MiniAppBridgeErrorModel(
                            "$ERR_GET_ENVIRONMENT_INFO ${e.message}")
                    )
            )
        }
    }

    internal companion object {
        const val ERR_GET_ENVIRONMENT_INFO = "Cannot get host environment info:"
    }
}
