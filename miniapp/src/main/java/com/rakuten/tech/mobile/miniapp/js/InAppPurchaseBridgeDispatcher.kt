package com.rakuten.tech.mobile.miniapp.js

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.iap.InAppPurchaseProvider
import com.rakuten.tech.mobile.miniapp.iap.MiniAppIAPVerifier
import com.rakuten.tech.mobile.miniapp.iap.PurchasedProductResponse
import com.rakuten.tech.mobile.miniapp.iap.PurchasedProductResponseStatus
import com.rakuten.tech.mobile.miniapp.iap.MiniAppPurchaseRequest
import com.rakuten.tech.mobile.miniapp.iap.TransactionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

/** Check whether hostapp provides InAppPurchase dependency. */
@Suppress("EmptyCatchBlock", "SwallowedException")
private inline fun <T> whenHasInAppPurchase(callback: () -> T) {
    try {
        Class.forName("com.rakuten.tech.mobile.miniapp.iap.InAppPurchaseProvider")
        callback.invoke()
    } catch (e: ClassNotFoundException) {}
}

@Suppress("TooGenericExceptionCaught")
internal class InAppPurchaseBridgeDispatcher {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var miniAppId: String
    private var isMiniAppComponentReady = false
    private lateinit var inAppPurchaseProvider: InAppPurchaseProvider
    private lateinit var apiClient: ApiClient
    internal var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var miniAppIAPVerifier: MiniAppIAPVerifier

    fun setMiniAppComponents(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppId: String,
        apiClient: ApiClient,
        miniAppIAPVerifier: MiniAppIAPVerifier
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.miniAppId = miniAppId
        this.apiClient = apiClient
        this.miniAppIAPVerifier = miniAppIAPVerifier
        isMiniAppComponentReady = true
    }

    fun setInAppPurchaseProvider(iapProvider: InAppPurchaseProvider) {
        this.inAppPurchaseProvider = iapProvider
    }

    private fun <T> whenReady(callbackId: String, callback: () -> T) = whenHasInAppPurchase {
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
                if (miniAppIAPVerifier.verify(miniAppId, callbackObj.param.productId)) {
                    val successCallback = { response: PurchasedProductResponse ->
                        if (response.status == PurchasedProductResponseStatus.PURCHASED) {
                            val purchaseRequest = MiniAppPurchaseRequest(
                                platform = PLATFORM,
                                productId = response.purchasedProduct.product.id,
                                transactionState = TransactionState.PURCHASED.state,
                                transactionId = response.purchasedProduct.transactionId,
                                transactionDate = formatTransactionDate(response.purchasedProduct.transactionDate),
                                transactionReceipt = response.purchasedProduct.transactionReceipt,
                                purchaseToken = response.purchasedProduct.purchaseToken
                            )
                            notifyMiniApp(callbackId, purchaseRequest)
                        } else {
                            bridgeExecutor.postError(
                                callbackId,
                                "$ERR_IN_APP_PURCHASE ${response.status}"
                            )
                        }
                    }
                    inAppPurchaseProvider.purchaseProductWith(
                        callbackObj.param.productId,
                        successCallback,
                        createErrorCallback(callbackId)
                    )
                } else {
                    errorCallback(callbackId, ERR_PRODUCT_ID_INVALID)
                }
            } catch (e: Exception) {
                errorCallback(callbackId, e.message.toString())
            }
        }

    private fun createErrorCallback(callbackId: String) = { errMessage: String ->
        bridgeExecutor.postError(callbackId, "$ERR_IN_APP_PURCHASE $errMessage")
    }

    private fun errorCallback(callbackId: String, errMessage: String) {
        bridgeExecutor.postError(callbackId, "$ERR_IN_APP_PURCHASE $errMessage")
    }

    fun onGetPurchaseItems(callbackId: String) = whenReady(callbackId) {
        scope.launch {
            try {
                val purchaseItemList = apiClient.fetchPurchaseItemList(miniAppId)
                bridgeExecutor.postValue(callbackId, Gson().toJson(purchaseItemList))
                miniAppIAPVerifier.storePurchaseItemsAsync(miniAppId, purchaseItemList)
            } catch (e: Exception) {
                errorCallback(callbackId, e.message.toString())
            }
        }
    }

    fun onConsumePurchase(callbackId: String, jsonStr: String) = whenReady(callbackId) {
        try {
            val callbackObj: ConsumePurchaseCallbackObj =
                Gson().fromJson(jsonStr, ConsumePurchaseCallbackObj::class.java)
            if (miniAppIAPVerifier.verify(miniAppId, callbackObj.param.productId)) {
                val successCallback = { _: PurchasedProductResponse ->
                }
                inAppPurchaseProvider.consumePurchaseWIth(
                    callbackObj.param.productId,
                    callbackObj.param.transactionId,
                    successCallback,
                    createErrorCallback(callbackId)
                )
            } else {
                errorCallback(callbackId, ERR_PRODUCT_ID_INVALID)
            }
        } catch (e: Exception) {
            errorCallback(callbackId, e.message.toString())
        }
    }

    private fun notifyMiniApp(callbackId: String, purchaseRequest: MiniAppPurchaseRequest) {
        scope.launch {
            try {
                val miniAppPurchaseResponse = apiClient.purchaseItem(miniAppId, purchaseRequest)
                bridgeExecutor.postValue(callbackId, Gson().toJson(miniAppPurchaseResponse))
            } catch (e: Exception) {
                errorCallback(callbackId, e.message.toString())
            }
        }
    }

    private fun formatTransactionDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        return format.format(date)
    }

    companion object {
        const val ERR_IN_APP_PURCHASE = "Cannot purchase item:"
        const val ERR_PRODUCT_ID_INVALID = "Invalid Product Id."
        const val PLATFORM = "ANDROID"
    }
}
