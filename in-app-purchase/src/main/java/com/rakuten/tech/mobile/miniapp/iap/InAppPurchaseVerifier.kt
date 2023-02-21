package com.rakuten.tech.mobile.miniapp.iap

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.android.billingclient.api.Purchase

internal class InAppPurchaseVerifier(context: Context) {
    @VisibleForTesting
    internal var miniAppPurchaseCache =
        InAppPurchaseCache(context, "com.rakuten.tech.mobile.miniapp.iap.cache")

    /** Verifies that the the product id is in cached data. */
    fun getPurchaseByTransactionId(transactionId: String): Purchase? =
        miniAppPurchaseCache.getPurchaseByTransactionId(transactionId)

    /** Stores IAP items in encrypted shared preferences. */
    suspend fun storePurchaseAsync(transactionId: String, items: List<Purchase>) =
        miniAppPurchaseCache.storePurchaseItemsAsync(transactionId, items)
}
