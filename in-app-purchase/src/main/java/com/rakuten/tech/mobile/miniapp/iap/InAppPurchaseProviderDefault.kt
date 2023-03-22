package com.rakuten.tech.mobile.miniapp.iap

import android.app.Activity
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.querySkuDetails
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ConsumeParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * This class acts as a default implementation of [InAppPurchaseProvider].
 * @param context should use the same activity context for #MiniAppDisplay.getMiniAppView.
 */
@Suppress("LargeClass", "TooManyFunctions")
class InAppPurchaseProviderDefault(
    private val context: Activity
) : InAppPurchaseProvider, CoroutineScope {
    private val job: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private var skuDetails: SkuDetails? = null
    private lateinit var onSuccess: (purchaseData: PurchaseData) -> Unit
    private lateinit var onConsumeSuccess: (title: String, description: String) -> Unit
    private lateinit var onError: (errorType: MiniAppInAppPurchaseErrorType) -> Unit

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                if (this::onError.isInitialized) onError(MiniAppInAppPurchaseErrorType.productPurchasedAlreadyError)
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                if (this::onError.isInitialized) onError(MiniAppInAppPurchaseErrorType.productNotFoundError)
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                if (this::onError.isInitialized) onError(MiniAppInAppPurchaseErrorType.userCancelledPurchaseError)
            } else if (this::onError.isInitialized) onError(MiniAppInAppPurchaseErrorType.purchaseFailedError)
        }

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    private fun <T> whenBillingClientReady(callback: () -> T) = startConnection { connected ->
        if (connected)
            callback.invoke()
        else
            onError(MiniAppInAppPurchaseErrorType.unknownError("Billing Client is not ready."))
    }

    private fun startConnection(callback: (connected: Boolean) -> Unit) {
        if (billingClient.isReady) {
            callback(true)
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        callback(true)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    callback(false)
                }
            })
        }
    }

    override fun getAllProducts(
        androidStoreIds: List<String>,
        onSuccess: (productInfos: List<ProductInfo>) -> Unit,
        onError: (errorType: MiniAppInAppPurchaseErrorType) -> Unit
    ) {
        whenBillingClientReady {
            launch {
                val products = getProductByIds(androidStoreIds)
                if (products.isNotEmpty()) onSuccess(products) else onError(
                    MiniAppInAppPurchaseErrorType.productNotFoundError
                )
            }
        }
    }

    override fun purchaseProductWith(
        androidStoreId: String,
        onSuccess: (purchaseData: PurchaseData) -> Unit,
        onError: (errorType: MiniAppInAppPurchaseErrorType) -> Unit
    ) {
        if (androidStoreId.isEmpty()) return

        this.onSuccess = onSuccess
        this.onError = onError
        startPurchasingProduct(androidStoreId)
    }

    override fun consumePurchaseWIth(
        purchaseToken: String,
        onSuccess: (title: String, description: String) -> Unit,
        onError: (errorType: MiniAppInAppPurchaseErrorType) -> Unit
    ) {
        this.onConsumeSuccess = onSuccess
        this.onError = onError
        startConsumingPurchase(purchaseToken)
    }

    override fun onEndConnection() {
        billingClient.endConnection()
    }

    private suspend fun getProductByIds(ids: List<String>): List<ProductInfo> {
        return createProductListFromSKuDetailList(querySkuDetails(ids))
    }

    private suspend fun querySkuDetails(productIds: List<String>): List<SkuDetails> {
        var skuDetailsList = ArrayList<SkuDetails>()
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(productIds).setType(BillingClient.SkuType.INAPP)

        withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }.let {
            if (it.billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                !it.skuDetailsList.isNullOrEmpty()
            ) {
                skuDetailsList.addAll(it.skuDetailsList!!)
                return skuDetailsList
            }
        }
        return skuDetailsList
    }

    private fun createProductListFromSKuDetailList(skuDetailsList: List<SkuDetails>): List<ProductInfo> {
        var productInfoList = ArrayList<ProductInfo>()
        for (skuDetails in skuDetailsList) {
            val productPrice = ProductPrice(skuDetails.priceCurrencyCode, skuDetails.price)
            val productInfo = ProductInfo(
                skuDetails.sku, skuDetails.title, skuDetails.description, productPrice
            )
            productInfoList.add(productInfo)
        }
        return productInfoList
    }

    private fun startPurchasingProduct(productId: String) = whenBillingClientReady {
        launchPurchaseFlow(androidStoreId = productId)
    }

    private fun launchPurchaseFlow(androidStoreId: String) {
        launch {
            skuDetails = querySkuDetails(listOf(androidStoreId)).first()
            skuDetails?.let {
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(it)
                    .build()
                billingClient.launchBillingFlow(context, flowParams).responseCode
            } ?: run {
                onError(MiniAppInAppPurchaseErrorType.purchaseFailedError)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        skuDetails?.let {
            val product = createProductListFromSKuDetailList(listOf(it)).first()
            val purchasedProductInfo = PurchasedProductInfo(
                productInfo = product,
                transactionId = purchase.orderId,
                transactionDate = purchase.purchaseTime
            )
            purchase.purchaseState
            val purchaseData = PurchaseData(
                purchaseState = purchase.purchaseState,
                purchasedProductInfo = purchasedProductInfo,
                purchaseToken = purchase.purchaseToken,
                transactionReceipt = purchase.originalJson,
            )
            if (this::onError.isInitialized) onSuccess(purchaseData)
        } ?: run {
            if (this::onError.isInitialized)
                onError(MiniAppInAppPurchaseErrorType.purchaseFailedError)
        }
    }

    private fun startConsumingPurchase(productId: String) = whenBillingClientReady {
        launchConsumeFlow(purchaseToken = productId)
    }

    private fun launchConsumeFlow(purchaseToken: String) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        billingClient.consumeAsync(params) { billingResult, _ ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    onConsumeSuccess("Consume", "successful")
                }
                else -> onError(MiniAppInAppPurchaseErrorType.consumeFailedError)
            }
        }
    }
}
