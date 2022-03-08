package com.rakuten.tech.mobile.miniapp.iap

/**
 * Functionalities related to In-App purchase.
 */
interface InAppPurchaseBridgeDispatcher {

    fun purchaseItem(
        onSuccess: (purchasedProduct: PurchasedProduct) -> Unit,
        onError: (message: String) -> Unit
    )
}

