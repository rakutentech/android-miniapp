package com.rakuten.tech.mobile.miniapp.iap

import android.content.Context
import androidx.annotation.VisibleForTesting

internal class MiniAppIAPVerifier(context: Context) {
    @VisibleForTesting
    internal var miniAppIAPCache =
        MiniAppIAPCache(context, "com.rakuten.tech.mobile.miniapp.iap.cache")

    /** Verifies that the the product id is in cached data. */
    fun verify(appId: String, productId: String): Boolean =
        miniAppIAPCache.verify(appId, productId)

    /** Stores IAP items in encrypted shared preferences. */
    suspend fun storePurchaseItemsAsync(appId: String, items: List<PurchaseItem>) =
        miniAppIAPCache.storePurchaseItemsAsync(appId, items)
}
