package com.rakuten.tech.mobile.miniapp.iap

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor

internal class InAppPurchaseBridge {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var miniAppId: String
    private var isMiniAppComponentReady = false
    private lateinit var inAppPurchaseBridgeDispatcher: InAppPurchaseBridgeDispatcher

    fun setMiniAppComponents(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppId: String
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.miniAppId = miniAppId
        isMiniAppComponentReady = true
    }

    fun setIAPBridgeDispatcher(dispatcher: InAppPurchaseBridgeDispatcher) {
        this.inAppPurchaseBridgeDispatcher = dispatcher
    }

    private fun <T> whenReady(callbackId: String, callback: () -> T) {
        if (isMiniAppComponentReady) {
            if (this::inAppPurchaseBridgeDispatcher.isInitialized)
                callback.invoke()
            else
                bridgeExecutor.postError(callbackId, ErrorBridgeMessage.NO_IMPL)
        }
    }

    internal fun onPurchaseItem(callbackId: String, jsonStr: String) =
        whenReady(callbackId) {
            try {
                val successCallback = { purchasedProduct: PurchasedProduct ->
                    bridgeExecutor.postValue(callbackId, Gson().toJson(purchasedProduct))
                }
                inAppPurchaseBridgeDispatcher.purchaseItem(
                    successCallback,
                    createErrorCallback(callbackId)
                )
            } catch (e: Exception) {
                bridgeExecutor.postError(callbackId, "$ERR_IN_APP_PURCHASE ${e.message}")
            }
        }

    private fun createErrorCallback(callbackId: String) = { errMessage: String ->
        bridgeExecutor.postError(callbackId, "$ERR_IN_APP_PURCHASE $errMessage")
    }

    internal companion object {
        const val ERR_IN_APP_PURCHASE = "Cannot purchase item:"
    }
}
