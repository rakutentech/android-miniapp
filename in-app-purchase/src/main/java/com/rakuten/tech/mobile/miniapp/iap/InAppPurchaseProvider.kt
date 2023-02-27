package com.rakuten.tech.mobile.miniapp.iap

/**
 * Functionalities related to In-App purchase.
 */
interface InAppPurchaseProvider {

    /**
     * Triggered when user wants to purchase an item.
     * [productId] item to be purchased.
     * Should invoke [onSuccess] with [PurchasedProductResponse] when user can purchase an item successfully.
     * Should invoke [onError] when there was an error.
     */
    fun purchaseProductWith(
        productId: String,
        onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
        onError: (message: String) -> Unit
    )

    /**
     * Triggered when user wants to consume an purchased item.
     * [productId] item to be consumed.
     * [transactionId] of the purchased item.
     * Should invoke [onSuccess] with [PurchasedProductResponse] when user can purchase an item successfully.
     * Should invoke [onError] when there was an error.
     */
    fun consumePurchaseWIth(
        productId: String,
        transactionId: String,
        onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
        onError: (message: String) -> Unit
    )

    /**
     * Triggered when user wants to end the connection with billing client.
     */
    fun onEndConnection()
}
