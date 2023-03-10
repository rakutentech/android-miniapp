package com.rakuten.tech.mobile.miniapp.js.iap

internal data class MiniAppPurchaseResponse(
    val productId: String,
    val transactionToken: String,
    val transactionState: String
)

internal enum class TransactionState(val state: Int) {
    PURCHASED(0),
    CANCELLED(1),
    PENDING(2),
    DEFAULT(-1);

    companion object {
        fun getState(state: String): TransactionState {
            return when (state) {
                "PURCHASED" -> PURCHASED
                "CANCELLED" -> CANCELLED
                "PENDING" -> PENDING
                else -> DEFAULT
            }
        }
    }
}
