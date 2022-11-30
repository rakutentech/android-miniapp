package com.rakuten.tech.mobile.miniapp.iap

internal data class MiniAppPurchaseRequest(
    val platform: String,
    val productId: String,
    val transactionState: Int,
    val transactionId: String?,
    val transactionDate: String?,
    val transactionReceipt: String?,
    val purchaseToken: String?
)
