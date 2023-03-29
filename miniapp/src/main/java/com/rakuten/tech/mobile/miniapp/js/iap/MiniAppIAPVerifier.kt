package com.rakuten.tech.mobile.miniapp.js.iap

import android.content.Context
import androidx.annotation.VisibleForTesting

internal class MiniAppIAPVerifier(context: Context) {
    @VisibleForTesting
    internal var miniAppIAPCache =
        MiniAppIAPCache(context, "com.rakuten.tech.mobile.miniapp.iap.cache")

    /** Verifies that the cached data for the Mini App manifest have not been modified. */
    fun verify(appId: String, productId: String): Boolean =
        miniAppIAPCache.verify(appId, productId)

    /** Stores purchasable items in encrypted shared preferences. */
    fun storePurchaseItems(appId: String, items: List<PurchaseItem>) =
        miniAppIAPCache.storePurchaseItems(appId, items)

    /** get the product id by android store id. */
    fun getProductIdByStoreId(appId: String, androidStoreId: String): String =
        miniAppIAPCache.getProductIdByStoreId(appId, androidStoreId)

    /** get the store id by android product id. */
    fun getStoreIdByProductId(appId: String, productId: String): String =
        miniAppIAPCache.getStoreIdByProductId(appId, productId)

    /** Stores IAP purchase record in encrypted shared preferences. */
    fun storePurchaseRecord(
        appId: String,
        androidStoreId: String,
        transactionId: String,
        item: MiniAppPurchaseRecordCache
    ) =
        miniAppIAPCache.storePurchaseRecord(appId, androidStoreId, transactionId, item)

    /** get the IAP purchase record. */
    fun getPurchaseRecordCache(
        appId: String,
        androidStoreId: String,
        transactionId: String
    ): MiniAppPurchaseRecordCache? =
        miniAppIAPCache.getPurchaseRecord(appId, androidStoreId, transactionId)
}
