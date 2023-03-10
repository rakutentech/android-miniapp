package com.rakuten.tech.mobile.miniapp.js.iap

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppResponseInfo
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.iap.InAppPurchaseProvider
import com.rakuten.tech.mobile.miniapp.iap.ProductInfo
import com.rakuten.tech.mobile.miniapp.iap.PurchasedProductResponse
import com.rakuten.tech.mobile.miniapp.iap.PurchasedProductResponseStatus
import com.rakuten.tech.mobile.miniapp.iap.PurchasedProductInfo
import com.rakuten.tech.mobile.miniapp.js.ConsumePurchaseCallbackObj
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.js.PurchasedProductCallbackObj
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

@Suppress("TooGenericExceptionCaught", "TooManyFunctions", "LargeClass")
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

    fun onGetPurchaseItems(callbackId: String) = whenReady(callbackId) {
        scope.launch {
            try {
                val purchaseItemList = apiClient.fetchPurchaseItemList(miniAppId)
                if (purchaseItemList.isNotEmpty()) {
                    val listOfProductIds = purchaseItemList.map { it.androidStoreId }
                    val successCallback = { productInfos: List<ProductInfo> ->
                        scope.launch {
                            miniAppIAPVerifier.storePurchaseItemsAsync(miniAppId, productInfos)
                        }
                        bridgeExecutor.postValue(callbackId, Gson().toJson(productInfos))
                    }
                    inAppPurchaseProvider.getAllProducts(
                        listOfProductIds, successCallback, createErrorCallback(callbackId)
                    )
                }
            } catch (e: Exception) {
                errorCallback(callbackId, e.message.toString())
            }
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
                            val miniAppPurchaseRecord = MiniAppPurchaseRecord(
                                platform = PLATFORM,
                                productId = response.purchasedProductInfo.productInfo.id,
                                transactionState = TransactionState.PURCHASED.state,
                                transactionId = response.purchasedProductInfo.transactionId,
                                transactionDate = formatTransactionDate(response.purchasedProductInfo.transactionDate),
                                transactionReceipt = response.transactionReceipt,
                                purchaseToken = response.purchaseToken
                            )
                            recordPurchase(callbackId, response.purchasedProductInfo, miniAppPurchaseRecord)
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

    @Suppress("LongMethod", "ComplexCondition")
    fun onConsumePurchase(callbackId: String, jsonStr: String) = whenReady(callbackId) {
        try {
            val callbackObj: ConsumePurchaseCallbackObj =
                Gson().fromJson(jsonStr, ConsumePurchaseCallbackObj::class.java)
            val record =
                miniAppIAPVerifier.getPurchaseRecordCache(
                    miniAppId,
                    callbackObj.param.productId,
                    callbackObj.param.productTransactionId
                )

            if (miniAppIAPVerifier.verify(miniAppId, callbackObj.param.productId) &&
                record != null &&
                record.transactionState == TransactionState.PURCHASED &&
                record.consumeStatus == ConsumeStatus.NOT_CONSUMED
            ) {

                val successCallback = { title: String, description: String ->
                    scope.launch {
                        updatePurchaseRecordCache(
                            record.miniAppPurchaseRecord.productId,
                            record.miniAppPurchaseRecord.transactionId,
                            record.miniAppPurchaseRecord,
                            record.purchasedRecordStatus,
                            ConsumeStatus.CONSUMED,
                            record.transactionState
                        )
                    }
                    bridgeExecutor.postValue(
                        callbackId,
                        Gson().toJson(MiniAppResponseInfo(title, description))
                    )
                }
                inAppPurchaseProvider.consumePurchaseWIth(
                    record.miniAppPurchaseRecord.purchaseToken,
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

    private fun recordPurchase(
        callbackId: String,
        purchasedProductInfo: PurchasedProductInfo,
        miniAppPurchaseRecord: MiniAppPurchaseRecord
    ) {
        scope.launch {
            try {
                updatePurchaseRecordCache(
                    purchasedProductInfo.productInfo.id,
                    miniAppPurchaseRecord.transactionId,
                    miniAppPurchaseRecord,
                    PurchasedRecordStatus.NOT_RECORDED,
                    ConsumeStatus.NOT_CONSUMED,
                    TransactionState.DEFAULT // NOT SURE
                )
                val miniAppPurchaseResponse = apiClient.purchaseItem(miniAppId, miniAppPurchaseRecord)
                updatePurchaseRecordCache(
                    purchasedProductInfo.productInfo.id,
                    miniAppPurchaseRecord.transactionId,
                    miniAppPurchaseRecord,
                    PurchasedRecordStatus.RECORDED,
                    ConsumeStatus.NOT_CONSUMED,
                    TransactionState.getState(miniAppPurchaseResponse.transactionState)
                )
                bridgeExecutor.postValue(callbackId, Gson().toJson(purchasedProductInfo))
            } catch (e: Exception) {
                updatePurchaseRecordCache(
                    purchasedProductInfo.productInfo.id,
                    miniAppPurchaseRecord.transactionId,
                    miniAppPurchaseRecord,
                    PurchasedRecordStatus.NOT_RECORDED,
                    ConsumeStatus.NOT_CONSUMED,
                    TransactionState.PENDING // NOT SURE
                )
                errorCallback(callbackId, e.message.toString())
            }
        }
    }

    @Suppress("LongParameterList")
    private suspend fun updatePurchaseRecordCache(
        productId: String,
        transactionId: String,
        miniAppPurchaseRecord: MiniAppPurchaseRecord,
        purchasedRecordStatus: PurchasedRecordStatus,
        consumeStatus: ConsumeStatus,
        transactionState: TransactionState
    ) {
        miniAppIAPVerifier.storePurchaseRecordAsync(
            miniAppId,
            productId,
            transactionId,
            MiniAppPurchaseRecordCache(
                miniAppPurchaseRecord = miniAppPurchaseRecord,
                purchasedRecordStatus = purchasedRecordStatus,
                consumeStatus = consumeStatus,
                transactionState = transactionState
            )
        )
    }

    private fun createErrorCallback(callbackId: String) = { errMessage: String ->
        bridgeExecutor.postError(callbackId, "$ERR_IN_APP_PURCHASE $errMessage")
    }

    private fun errorCallback(callbackId: String, errMessage: String) {
        bridgeExecutor.postError(callbackId, "$ERR_IN_APP_PURCHASE $errMessage")
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
