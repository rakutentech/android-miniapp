package com.rakuten.tech.mobile.miniapp.iap

import android.app.Activity
import com.android.billingclient.api.*

/**
 * The In-App purchase executor which acts as a default implementation of InAppPurchaseProvider.
 * @param context should use the same activity context for #MiniAppDisplay.getMiniAppView.
 */
class InAppPurchaseExecutor(private val context: Activity) : InAppPurchaseProvider {
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchases(purchase)
                }
            }
        }

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    private var skuDetails: SkuDetails? = null
    private lateinit var onSuccessEx: (purchasedProduct: PurchasedProduct) -> Unit
    private lateinit var onErrorEx: (message: String) -> Unit

    private fun startPurchasingProduct(itemID: String) {
        startConnection(itemID)
    }

    private fun startConnection(productID: String) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails(productID)
                }
            }

            override fun onBillingServiceDisconnected() {
                onErrorEx("Billing service has been disconnected.")
            }
        })
    }

    private fun querySkuDetails(productID: String) {
        val skuList = ArrayList<String>()
        skuList.add(productID)
        val params = SkuDetailsParams.newBuilder()

        // proceed with In-App type
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                for (skuDetails in skuDetailsList) {
                    this.skuDetails = skuDetails
                }
                launchPurchaseFlow()
            } else {
                onErrorEx("There is an error happened while purchasing item.")
            }
        }
    }

    private fun launchPurchaseFlow() {
        skuDetails?.let {
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient.launchBillingFlow(context, billingFlowParams).responseCode
        }
    }

    private fun handlePurchases(purchase: Purchase) {
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
                        val purchasedProduct = PurchasedProduct(product, "", "")
                        onSuccessEx(purchasedProduct)
                    }
                }
                else -> {
                    onErrorEx(billingResult.debugMessage)
                }
            }
        }
    }

    override fun purchaseItem(
        itemId: String,
        onSuccess: (purchasedProduct: PurchasedProduct) -> Unit,
        onError: (message: String) -> Unit
    ) {
        onSuccessEx = onSuccess
        onErrorEx = onError
        startPurchasingProduct(itemId)
    }
}
