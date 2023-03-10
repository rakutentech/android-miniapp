package com.rakuten.tech.mobile.miniapp.js.iap

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.iap.ProductInfo

internal class MiniAppIAPVerifier(context: Context) {
    @VisibleForTesting
    internal var miniAppIAPCache =
        MiniAppIAPCache(context, "com.rakuten.tech.mobile.miniapp.iap.cache")

    /** Verifies that the the product id is in cached data. */
    fun verify(appId: String, productId: String): Boolean =
        miniAppIAPCache.verify(appId, productId)

    /** Stores IAP items in encrypted shared preferences. */
    suspend fun storePurchaseItemsAsync(appId: String, items: List<ProductInfo>) =
        miniAppIAPCache.storePurchaseItemsAsync(appId, items)

    /** Stores IAP purchase record in encrypted shared preferences. */
    suspend fun storePurchaseRecordAsync(
        appId: String,
        productId: String,
        transactionId: String,
        item: MiniAppPurchaseRecordCache
    ) =
        miniAppIAPCache.storePurchaseRecordAsync(appId, productId, transactionId, item)

    /** Stores IAP purchase record. */
    fun getPurchaseRecordCache(appId: String, productId: String, transactionId: String): MiniAppPurchaseRecordCache? =
        miniAppIAPCache.getPurchaseRecord(appId, productId, transactionId)
}
