package com.rakuten.tech.mobile.miniapp.iap

/**
 * Functionalities related to In-App purchase.
 */
interface InAppPurchaseProvider {

    /**
     * Triggered when Mini App wants purchase an item.
     * Should invoke [onSuccess] with PurchasedProduct when user can purchase an item successfully.
     * Should invoke [onError] when there was an error.
     */
    fun purchaseItem(
        itemId: String,
        onSuccess: (purchasedProduct: PurchasedProduct) -> Unit,
        onError: (message: String) -> Unit
    )
}
