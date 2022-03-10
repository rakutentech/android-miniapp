package com.rakuten.tech.mobile.testapp.ui.iap

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import com.rakuten.tech.mobile.testapp.helper.showAlertDialog

class IAPExecutor(val activity: Activity) {
    private var billingClient: BillingClient? = null
    private var skuDetails: SkuDetails? = null

    fun startPurchasingProduct(itemID: String) {
        setUpBillingClient(itemID)
        retrieveSkuDetails()
    }

    fun getProductDetails() = skuDetails

    private fun setUpBillingClient(productID: String) {
        billingClient = BillingClient.newBuilder(activity)
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

            override fun onBillingServiceDisconnected() {}
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
                showAlertDialog(
                    activity,
                    "Warning!",
                    "There is an error happened while purchasing item"
                )
            }
        }
    }

    private fun retrieveSkuDetails() {
        skuDetails?.let {
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient?.launchBillingFlow(activity, billingFlowParams)?.responseCode
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

    private companion object {
        val TAG: String? = IAPExecutor::class.java.canonicalName
    }
}
