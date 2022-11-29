package com.rakuten.tech.mobile.miniapp.iap

data class MiniAppPurchaseResponse(
    val productId: String,
    val transactionToken: String,
    val transactionState: String
)

enum class TransactionState(val state: Int) {
    PURCHASED(0),
    CANCELLED(1),
    PENDING(2),
    DEFAULT(-1)
}
