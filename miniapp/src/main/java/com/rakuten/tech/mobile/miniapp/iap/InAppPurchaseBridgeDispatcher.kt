package com.rakuten.tech.mobile.miniapp.iap

import android.util.Log
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.js.PurchasedProductCallbackObj

@Suppress("TooGenericExceptionCaught")
internal class InAppPurchaseBridgeDispatcher {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var miniAppId: String
    private var isMiniAppComponentReady = false
    private lateinit var inAppPurchaseProvider: InAppPurchaseProvider

    fun setMiniAppComponents(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppId: String
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.miniAppId = miniAppId
        isMiniAppComponentReady = true
    }

    fun setInAppPurchaseProvider(iapProvider: InAppPurchaseProvider) {
        this.inAppPurchaseProvider = iapProvider
    }

    private fun <T> whenReady(callbackId: String, callback: () -> T) {
        if (isMiniAppComponentReady) {
            if (this::inAppPurchaseProvider.isInitialized)
                callback.invoke()
            else
                bridgeExecutor.postError(callbackId, ErrorBridgeMessage.NO_IMPL)
        }
    }

    fun onPurchaseItem(callbackId: String, jsonStr: String) =
        whenReady(callbackId) {
            try {
                val callbackObj: PurchasedProductCallbackObj =
                    Gson().fromJson(jsonStr, PurchasedProductCallbackObj::class.java)
                val successCallback = { response: PurchasedProductResponse ->
                    bridgeExecutor.postValue(callbackId, Gson().toJson(response))
                }
                Log.d("AAAA jsonStr", jsonStr)
                inAppPurchaseProvider.purchaseItem(
                    callbackObj.param.itemId,
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

    companion object {
        const val ERR_IN_APP_PURCHASE = "Cannot purchase item:"
    }
}
