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

    private var skuDetails: SkuDetails? = null
    private lateinit var onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit
    private lateinit var onError: (message: String) -> Unit
    private val inAppPurchaseVerifier = InAppPurchaseVerifier(context)

    private fun <T> whenBillingClientReady(callback: () -> T) = startConnection { connected ->
        if (connected)
            callback.invoke()
        else
            onError(BILLING_SERVICE_DISCONNECTED)
    }

    private fun startPurchasingProduct(productId: String) = whenBillingClientReady {
        launchPurchaseFlow(productId = productId)
    }

    private fun startConsumingPurchase(productId: String, transactionId: String) = whenBillingClientReady {
        launchConsumeFlow(productId = productId, transactionId = transactionId)
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

    private suspend fun querySkuDetails(productId: String): SkuDetails? {
        val skuList = ArrayList<String>()
        skuList.add(productId)
        val params = SkuDetailsParams.newBuilder()
        // proceed with In-App type
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }.let {
            if (it.billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                !it.skuDetailsList.isNullOrEmpty()
            ) {
                for (skuDetails in it.skuDetailsList!!) {
                    return skuDetails
                }
            } else {
               return null
            }
        }
        return null
    }

    private fun launchPurchaseFlow(productId: String) {
        launch {
            skuDetails = querySkuDetails(productId)
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
            launch {
                inAppPurchaseVerifier.storePurchaseAsync(purchase.orderId, purchase)
            }
            val productPrice = ProductPrice(it.priceCurrencyCode, it.price)
            val product = Product(
                it.sku, it.title, it.description, productPrice
            )
            val purchasedProduct = PurchasedProduct(
                product = product,
                transactionId = purchase.orderId,
                purchaseToken = purchase.purchaseToken,
                transactionReceipt = purchase.originalJson,
                transactionDate = purchase.purchaseTime
            )
            val purchasedProductResponse = PurchasedProductResponse(
                PurchasedProductResponseStatus.PURCHASED,
                purchasedProduct
            )
            onSuccess(purchasedProductResponse)
        } ?: run {
            onError(ERR_PURCHASING_ITEM)
        }
    }

    private fun launchConsumeFlow(productId: String, transactionId: String) {
        launch {
            skuDetails = querySkuDetails(productId)
            skuDetails?.let {
                val purchase = inAppPurchaseVerifier.getPurchaseByTransactionId(transactionId)
                if (purchase != null) {
                    val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                    billingClient.consumeAsync(params) { billingResult, _ ->
                        when (billingResult.responseCode) {
                            BillingClient.BillingResponseCode.OK -> {
                                // prepare the product to be invoked using onSuccess
                                skuDetails?.let {
                                    val productPrice = ProductPrice(it.priceCurrencyCode, it.price)
                                    val product = Product(
                                        it.sku, it.title, it.description, productPrice
                                    )
                                    val purchasedProduct = PurchasedProduct(
                                        product = product,
                                        transactionId = purchase.orderId,
                                        purchaseToken = purchase.purchaseToken,
                                        transactionReceipt = purchase.originalJson,
                                        transactionDate = purchase.purchaseTime
                                    )
                                    val purchasedProductResponse = PurchasedProductResponse(
                                        PurchasedProductResponseStatus.PURCHASED,
                                        purchasedProduct
                                    )
                                    onSuccess(purchasedProductResponse)
                                }
                            }
                            else -> onError(billingResult.debugMessage)
                        }
                    }
                } else
                    onError(ERR_CONSUME_PURCHASE)
            } ?: run {
                onError(ERR_CONSUME_PURCHASE)
            }
        }
    }

    override fun purchaseProductWith(
        productId: String,
        onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (productId.isEmpty()) return

        this.onSuccess = onSuccess
        this.onError = onError
        startPurchasingProduct(productId)
    }

    override fun consumePurchaseWIth(
        productId: String,
        transactionId: String,
        onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
        onError: (message: String) -> Unit
    ) {
        this.onSuccess = onSuccess
        this.onError = onError
        startConsumingPurchase(productId, transactionId)
    }

    override fun onEndConnection() {
        billingClient.endConnection()
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
