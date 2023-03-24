package com.rakuten.tech.mobile.miniapp.js.iap

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppResponseInfo
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.errors.MiniAppBridgeErrorModel
import com.rakuten.tech.mobile.miniapp.iap.InAppPurchaseProvider
import com.rakuten.tech.mobile.miniapp.iap.MiniAppInAppPurchaseErrorType
import com.rakuten.tech.mobile.miniapp.iap.ProductInfo
import com.rakuten.tech.mobile.miniapp.iap.PurchaseData
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
    } catch (e: ClassNotFoundException) {
        Log.e("Missing Dependency", ":in-app-purchase")
    }
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
                    miniAppIAPVerifier.storePurchaseItems(miniAppId, purchaseItemList)
                    val listOfAndroidStoreIds = purchaseItemList.map { it.androidStoreId }
                    val successCallback = { productInfo: List<ProductInfo> ->
                        // Replace android store id with product id
                        productInfo.map { it.id = miniAppIAPVerifier.getProductIdByStoreId(miniAppId, it.id) }
                        bridgeExecutor.postValue(callbackId, Gson().toJson(productInfo))
                    }
                    val errorCallback = { error: MiniAppInAppPurchaseErrorType ->
                        val errorBridgeModel = MiniAppBridgeErrorModel(error.type, error.message)
                        bridgeExecutor.postError(callbackId, Gson().toJson(errorBridgeModel))
                    }
                    inAppPurchaseProvider.getAllProducts(
                        listOfAndroidStoreIds, successCallback, errorCallback
                    )
                } else {
                    genericErrorCallback(callbackId, ERR_EMPTY_LIST)
                }
            } catch (e: Exception) {
                genericErrorCallback(callbackId, e.message.toString())
            }
        }
    }

    @Suppress("LongMethod")
    fun onPurchaseItem(callbackId: String, jsonStr: String) =
        whenReady(callbackId) {
            try {
                val callbackObj: PurchasedProductCallbackObj =
                    Gson().fromJson(jsonStr, PurchasedProductCallbackObj::class.java)
                val androidStoreId = miniAppIAPVerifier.getStoreIdByProductId(miniAppId, callbackObj.param.productId)
                if (androidStoreId.isNotEmpty()) {
                    val successCallback = { response: PurchaseData ->
                            // Create the record to send to platform
                            val miniAppPurchaseRecord = MiniAppPurchaseRecord(
                                platform = PLATFORM,
                                productId = response.purchasedProductInfo.productInfo.id,
                                transactionState = response.purchaseState,
                                transactionId = response.purchasedProductInfo.transactionId,
                                transactionDate = formatTransactionDate(response.purchasedProductInfo.transactionDate),
                                transactionReceipt = response.transactionReceipt,
                                purchaseToken = response.purchaseToken
                            )
                            recordPurchase(
                                androidStoreId = response.purchasedProductInfo.productInfo.id,
                                miniAppPurchaseRecord = miniAppPurchaseRecord
                            ) { _, _ ->
                                // Replace android store id with product id
                                response.purchasedProductInfo.productInfo.id =
                                    miniAppIAPVerifier.getProductIdByStoreId(
                                        miniAppId,
                                        response.purchasedProductInfo.productInfo.id
                                    )
                                bridgeExecutor.postValue(
                                    callbackId,
                                    Gson().toJson(response.purchasedProductInfo)
                                )
                            }
                    }

                    val errorCallback = { error: MiniAppInAppPurchaseErrorType ->
                        val errorBridgeModel = MiniAppBridgeErrorModel(error.type, error.message)
                        bridgeExecutor.postError(callbackId, Gson().toJson(errorBridgeModel))
                    }

                    inAppPurchaseProvider.purchaseProductWith(
                        androidStoreId,
                        successCallback,
                        errorCallback
                    )
                } else {
                    genericErrorCallback(callbackId, ERR_PRODUCT_ID_INVALID)
                }
            } catch (e: Exception) {
                genericErrorCallback(callbackId, e.message.toString())
            }
        }

    @Suppress("LongMethod", "ComplexCondition", "ComplexMethod")
    fun onConsumePurchase(callbackId: String, jsonStr: String) = whenReady(callbackId) {
        try {
            val callbackObj: ConsumePurchaseCallbackObj =
                Gson().fromJson(jsonStr, ConsumePurchaseCallbackObj::class.java)
            val androidStoreId = miniAppIAPVerifier.getStoreIdByProductId(miniAppId, callbackObj.param.productId)
            val record = miniAppIAPVerifier.getPurchaseRecordCache(
                miniAppId,
                androidStoreId,
                callbackObj.param.productTransactionId
            )
            if (record != null) {
                when (checkPurchaseStatus(record)) {
                    State.RECORDED_NOT_CONSUMED -> consumePurchase(callbackId, record)
                    State.NOT_RECORDED_PURCHASED -> recordPurchase(
                        androidStoreId = record.miniAppPurchaseRecord.productId,
                        miniAppPurchaseRecord = record.miniAppPurchaseRecord
                    ) { isRecorded, errorMsg ->
                        if (isRecorded)
                            consumePurchase(callbackId, record)
                        else
                            genericErrorCallback(callbackId, errorMsg ?: "")
                    }
                    State.PENDING_PURCHASE -> checkPurchaseState(record) { state ->
                        handleConsume(state, callbackId, record)
                    }
                    State.CANCEL_PURCHASE -> genericErrorCallback(callbackId, ERR_CANCEL_PURCHASE)
                }
            } else {
                genericErrorCallback(callbackId, ERR_INVALID_PURCHASE)
            }
        } catch (e: Exception) {
            genericErrorCallback(callbackId, e.message.toString())
        }
    }

    private fun handleConsume(state: TransactionState, callbackId: String, record: MiniAppPurchaseRecordCache) {
        when (state) {
            TransactionState.PURCHASED -> consumePurchase(callbackId, record)
            TransactionState.CANCELLED -> genericErrorCallback(callbackId, ERR_CANCEL_PURCHASE)
            TransactionState.PENDING -> genericErrorCallback(callbackId, ERR_PENDING_PURCHASE)
        }
    }

    private fun consumePurchase(callbackId: String, record: MiniAppPurchaseRecordCache) {
        val successCallback = { title: String, description: String ->
            updatePurchaseRecordCache(
                androidStoreId = record.miniAppPurchaseRecord.productId,
                transactionId = record.miniAppPurchaseRecord.transactionId,
                miniAppPurchaseRecord = record.miniAppPurchaseRecord,
                platformRecordStatus = record.platformRecordStatus,
                productConsumeStatus = ProductConsumeStatus.CONSUMED,
                transactionState = record.transactionState,
                transactionToken = record.transactionToken
            )

            bridgeExecutor.postValue(
                callbackId,
                Gson().toJson(MiniAppResponseInfo(title, description))
            )
        }
        val errorCallback = { error: MiniAppInAppPurchaseErrorType ->
            val errorBridgeModel = MiniAppBridgeErrorModel(error.type, error.message)
            bridgeExecutor.postError(callbackId, Gson().toJson(errorBridgeModel))
        }
        inAppPurchaseProvider.consumePurchaseWIth(
            record.miniAppPurchaseRecord.purchaseToken,
            successCallback,
            errorCallback
        )
    }

    @Suppress("LongMethod")
    @VisibleForTesting
    internal fun recordPurchase(
        androidStoreId: String,
        miniAppPurchaseRecord: MiniAppPurchaseRecord,
        callback: (isRecorded: Boolean, errorMsg: String?) -> Unit
    ) {
        scope.launch {
            try {
                val miniAppPurchaseResponse = apiClient.recordPurchase(miniAppId, miniAppPurchaseRecord)
                updatePurchaseRecordCache(
                    androidStoreId = androidStoreId,
                    transactionId = miniAppPurchaseRecord.transactionId,
                    miniAppPurchaseRecord = miniAppPurchaseRecord,
                    platformRecordStatus = PlatformRecordStatus.RECORDED,
                    productConsumeStatus = ProductConsumeStatus.NOT_CONSUMED,
                    transactionState = TransactionState.getState(miniAppPurchaseResponse.transactionState),
                    transactionToken = miniAppPurchaseResponse.transactionToken
                )
                callback.invoke(true, null)
            } catch (e: Exception) {
                updatePurchaseRecordCache(
                    androidStoreId = androidStoreId,
                    transactionId = miniAppPurchaseRecord.transactionId,
                    miniAppPurchaseRecord = miniAppPurchaseRecord,
                    platformRecordStatus = PlatformRecordStatus.NOT_RECORDED,
                    productConsumeStatus = ProductConsumeStatus.NOT_CONSUMED,
                    transactionState = TransactionState.PENDING,
                    transactionToken = ""
                )
                callback.invoke(false, e.message.toString())
            }
        }
    }

    private fun checkPurchaseState(
        recordCache: MiniAppPurchaseRecordCache,
        callback: (state: TransactionState) -> Unit
    ) {
        scope.launch {
            try {
                val miniAppPurchaseState = apiClient.getTransactionStatus(
                    miniAppId,
                    recordCache.transactionToken
                )
                updatePurchaseRecordCache(
                    androidStoreId = recordCache.miniAppPurchaseRecord.productId,
                    transactionId = recordCache.miniAppPurchaseRecord.transactionId,
                    miniAppPurchaseRecord = recordCache.miniAppPurchaseRecord,
                    platformRecordStatus = recordCache.platformRecordStatus,
                    productConsumeStatus = recordCache.productConsumeStatus,
                    transactionState = TransactionState.getState(miniAppPurchaseState.transactionState),
                    transactionToken = miniAppPurchaseState.transactionToken
                )
                callback.invoke(TransactionState.getState(miniAppPurchaseState.transactionState))
            } catch (e: Exception) {
                updatePurchaseRecordCache(
                    androidStoreId = recordCache.miniAppPurchaseRecord.productId,
                    transactionId = recordCache.miniAppPurchaseRecord.transactionId,
                    miniAppPurchaseRecord = recordCache.miniAppPurchaseRecord,
                    platformRecordStatus = recordCache.platformRecordStatus,
                    productConsumeStatus = recordCache.productConsumeStatus,
                    transactionState = TransactionState.PENDING,
                    transactionToken = recordCache.transactionToken
                )
                callback.invoke(TransactionState.PENDING)
            }
        }
    }

    @Suppress("LongParameterList")
    private fun updatePurchaseRecordCache(
        androidStoreId: String,
        transactionId: String,
        miniAppPurchaseRecord: MiniAppPurchaseRecord,
        platformRecordStatus: PlatformRecordStatus,
        productConsumeStatus: ProductConsumeStatus,
        transactionState: TransactionState,
        transactionToken: String
    ) {
        miniAppIAPVerifier.storePurchaseRecord(
            miniAppId,
            androidStoreId,
            transactionId,
            MiniAppPurchaseRecordCache(
                miniAppPurchaseRecord = miniAppPurchaseRecord,
                platformRecordStatus = platformRecordStatus,
                productConsumeStatus = productConsumeStatus,
                transactionState = transactionState,
                transactionToken = transactionToken
            )
        )
    }

    private fun checkPurchaseStatus(record: MiniAppPurchaseRecordCache): State {
        return when {
            record.platformRecordStatus == PlatformRecordStatus.RECORDED &&
                    record.transactionState == TransactionState.PURCHASED &&
                    record.productConsumeStatus == ProductConsumeStatus.NOT_CONSUMED -> State.RECORDED_NOT_CONSUMED
            record.platformRecordStatus == PlatformRecordStatus.NOT_RECORDED &&
                    record.transactionState == TransactionState.PURCHASED &&
                    record.productConsumeStatus == ProductConsumeStatus.NOT_CONSUMED -> State.NOT_RECORDED_PURCHASED
            record.transactionState == TransactionState.PENDING -> State.PENDING_PURCHASE
            record.transactionState == TransactionState.CANCELLED -> State.CANCEL_PURCHASE
            else -> throw IllegalStateException("Invalid purchase state")
        }
    }

    private enum class State {
        RECORDED_NOT_CONSUMED,
        NOT_RECORDED_PURCHASED,
        PENDING_PURCHASE,
        CANCEL_PURCHASE
    }

    private fun genericErrorCallback(callbackId: String, errMessage: String) {
        bridgeExecutor.postError(callbackId, "$ERR_IN_APP_PURCHASE $errMessage")
    }

    private fun formatTransactionDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        return format.format(date)
    }

    internal fun disconnectIAPBillingClient() = whenHasInAppPurchase {
        inAppPurchaseProvider.onEndConnection()
    }

    companion object {
        const val ERR_IN_APP_PURCHASE = "InApp Purchase Error:"
        const val ERR_PRODUCT_ID_INVALID = "Invalid Product Id."
        const val ERR_INVALID_PURCHASE = "Invalid Purhcase."
        const val ERR_PENDING_PURCHASE = "Pending Purchase."
        const val ERR_CANCEL_PURCHASE = "Purchase Cancelled."
        const val ERR_EMPTY_LIST = "Empty product list."
        const val PLATFORM = "ANDROID"
    }
}
