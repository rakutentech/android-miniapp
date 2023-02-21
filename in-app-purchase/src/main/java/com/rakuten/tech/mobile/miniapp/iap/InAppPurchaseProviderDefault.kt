package com.rakuten.tech.mobile.miniapp.iap

import android.app.Activity
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.pow

/**
 * This class acts as a default implementation of [InAppPurchaseProvider].
 * @param context should use the same activity context for #MiniAppDisplay.getMiniAppView.
 */
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

    private fun startPurchasingProduct(itemID: String) = startConnection(itemID)

    private fun startConnection(productID: String) {
        if (billingClient.isReady) {
            launch {
                val skuDetails = querySkuDetails(productID)
                if (skuDetails != null)
                    launchPurchaseFlow()
                else
                    onError(ERR_PURCHASING_ITEM)
            }
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        launch {
                            val skuDetails = querySkuDetails(productID)
                            if (skuDetails != null)
                                launchPurchaseFlow()
                            else
                                onError(ERR_PURCHASING_ITEM)
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    launch {
                        retryConnection(productID)
                    }
                }
            })
        }
    }

    @Suppress("FunctionParameterNaming")
    private suspend fun retryConnection(productID: String, _retryCount: Int = 0) {
        startConnection(productID)

        var retryCount = _retryCount
        if (retryCount++ < TOTAL_RETRIES) {
            retryCount++
            delay(getWaitingTime(retryCount))
            retryConnection(productID, retryCount)
        } else {
            onError(BILLING_SERVICE_DISCONNECTED)
        }
    }

    @Suppress("MagicNumber")
    private fun getWaitingTime(retryCount: Int): Long {
        val backOff = 2.0
        val waitTime = 1000 * 0.5 * backOff.pow(retryCount.toDouble())
        return waitTime.toLong()
    }

    private suspend fun querySkuDetails(productID: String): SkuDetails? {
        val skuList = ArrayList<String>()
        skuList.add(productID)
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
                    this.skuDetails = skuDetails
                    return skuDetails
                }
            } else {
               return null
            }
        }
        return null
    }

    private fun launchPurchaseFlow() {
        skuDetails?.let {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient.launchBillingFlow(context, flowParams).responseCode
        }
    }

    private fun handlePurchase(purchase: Purchase) {
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

    private fun consumePurcahse(productID: String, purchase: Purchase){
        launch {
            val skuDetails = querySkuDetails(productID)
            if (skuDetails != null)
                launchConsumeFlow(purchase)
            else
                onError(ERR_PURCHASING_ITEM)
        }
    }

    private fun launchConsumeFlow(purchase: Purchase) {
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
                } else -> onError(billingResult.debugMessage)
            }
        }

    }

    override fun purchaseProductWith(
        product_id: String,
        onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (product_id.isEmpty()) return

        this.onSuccess = onSuccess
        this.onError = onError
        startPurchasingProduct(product_id)
    }

    override fun consumePurchaseWIth(
        product_id: String,
        transaction_id: String,
        onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
        onError: (message: String) -> Unit
    ) {
        this.onSuccess = onSuccess
        this.onError = onError
        val purchase = inAppPurchaseVerifier.getPurchaseByTransactionId(transaction_id)
        if (purchase != null)
            consumePurcahse(product_id, purchase)
        else
            onError(ERR_CONSUME_PURCHASE)
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
