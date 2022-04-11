package com.rakuten.tech.mobile.miniapp.iap

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*

/**
 * The In-App purchase executor which acts as a default implementation of InAppPurchaseProvider.
 * @param context should use the same activity context for #MiniAppDisplay.getMiniAppView.
 */
class InAppPurchaseExecutor(private val context: Activity) : InAppPurchaseProvider {
    private var billingClient: BillingClient? = null
    private var skuDetails: SkuDetails? = null

    private fun startPurchasingProduct(itemID: String) {
        setUpBillingClient(itemID)
        retrieveSkuDetails()
    }

    private fun setUpBillingClient(productID: String) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()

        startConnection(productID)
    }

    private fun startConnection(productID: String) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAvailableProducts(productID)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service has been disconnected.")
            }
        })
    }

    private fun queryAvailableProducts(productID: String) {
        val skuList = ArrayList<String>()
        skuList.add(productID)
        val params = SkuDetailsParams.newBuilder()

        // proceed with In-App type
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        billingClient?.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                for (skuDetails in skuDetailsList) {
                    this.skuDetails = skuDetails
                }
            } else {
                Log.e(TAG, "There is an error happened while purchasing item")
            }
        }
    }

    private fun retrieveSkuDetails() {
        skuDetails?.let {
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient?.launchBillingFlow(context, billingFlowParams)?.responseCode
        }
    }

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchases(purchase)
                }
            }
        }

    private fun handlePurchases(purchase: Purchase) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient?.consumeAsync(params) { billingResult, _ ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, " Update the appropriate dataset")
                }
                else -> {
                    Log.w(TAG, billingResult.debugMessage)
                }
            }
        }
    }

    override fun purchaseItem(
        itemId: String,
        onSuccess: (purchasedProduct: PurchasedProduct) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // purchasing In-App item using GooglePlay billing library
        startPurchasingProduct(itemId)

        // prepare the product to be invoked using onSuccess
        skuDetails?.let {
            val productPrice = ProductPrice(
                it.priceAmountMicros.toInt(), it.priceCurrencyCode, it.price
            )
            val product = Product(
                itemId, it.title, it.description, productPrice
            )
            val purchasedProduct = PurchasedProduct(
                "", product, ""
            )
            onSuccess(purchasedProduct)
        }
    }

    private companion object {
        val TAG: String? = InAppPurchaseExecutor::class.java.canonicalName
    }
}
