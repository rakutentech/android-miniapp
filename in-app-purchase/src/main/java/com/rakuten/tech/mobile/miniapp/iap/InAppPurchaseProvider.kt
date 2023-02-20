package com.rakuten.tech.mobile.miniapp.iap

/**
 * Functionalities related to In-App purchase.
 */
interface InAppPurchaseProvider {

    /**
     * Triggered when user wants to purchase an item.
     * [product_id] item to be purchased.
     * Should invoke [onSuccess] with [PurchasedProductResponse] when user can purchase an item successfully.
     * Should invoke [onError] when there was an error.
     */
    fun purchaseProductWith(
        product_id: String,
        onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
        onError: (message: String) -> Unit
    )

    /**
     * Triggered when user wants to consume an purchased item.
     * [product_id] item to be consumed.
     * [transaction_id] of the purchased item.
     * Should invoke [onSuccess] with [PurchasedProductResponse] when user can purchase an item successfully.
     * Should invoke [onError] when there was an error.
     */
    fun consumePurchaseWIth(
        product_id: String,
        transaction_id: String,
        onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
        onError: (message: String) -> Unit
    )

    /**
     * Triggered when user wants to end the connection with billing client.
     */
    fun onEndConnection()
}
