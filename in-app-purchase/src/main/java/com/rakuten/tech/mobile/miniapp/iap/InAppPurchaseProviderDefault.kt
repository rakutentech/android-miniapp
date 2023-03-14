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
    private lateinit var onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit
    private lateinit var onConsumeSuccess: (title: String, description: String) -> Unit
    private lateinit var onError: (message: String) -> Unit

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                onError(ERR_ITEM_ALREADY_OWNED)
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                onError(ERR_ITEM_UNAVAILABLE)
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                onError(ERR_USER_CANCELLED)
            } else onError(ERR_PURCHASING_ITEM)
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
            onError(BILLING_SERVICE_DISCONNECTED)
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
        onError: (message: String) -> Unit
    ) {
        whenBillingClientReady {
            launch {
                val products = getProductByIds(androidStoreIds)
                if (products.isNotEmpty()) onSuccess(products) else onError(ERR_ITEM_UNAVAILABLE)
            }
        }
    }

    override fun purchaseProductWith(
        androidStoreId: String,
        onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (androidStoreId.isEmpty()) return

        this.onSuccess = onSuccess
        this.onError = onError
        startPurchasingProduct(androidStoreId)
    }

    override fun consumePurchaseWIth(
        purchaseToken: String,
        onSuccess: (title: String, description: String) -> Unit,
        onError: (message: String) -> Unit
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
                onError(ERR_PURCHASING_ITEM)
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
            val purchasedProductResponse = PurchasedProductResponse(
                status = PurchasedProductResponseStatus.PURCHASED,
                purchasedProductInfo = purchasedProductInfo,
                purchaseToken = purchase.purchaseToken,
                transactionReceipt = purchase.originalJson,
            )
            onSuccess(purchasedProductResponse)
        } ?: run {
            onError(ERR_PURCHASING_ITEM)
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
                else -> onError(billingResult.debugMessage)
            }
        }
    }

    private companion object {
        private const val TOTAL_RETRIES = 5
        const val ERR_ITEM_ALREADY_OWNED = "This product has been already owned."
        const val ERR_ITEM_UNAVAILABLE = "This product is unavailable."
        const val ERR_USER_CANCELLED = "User has cancelled the purchase."
        const val ERR_PURCHASING_ITEM = "There is an error happened while purchasing item."
        const val ERR_CONSUME_PURCHASE = "There is an error happened while consuming purchase."
        const val BILLING_SERVICE_DISCONNECTED = "Billing service has been disconnected."
    }
}
