package com.rakuten.tech.mobile.miniapp.js.iap

internal data class MiniAppPurchaseRecord(
    val platform: String,
    val productId: String,
    val transactionState: Int,
    val transactionId: String,
    val transactionDate: String,
    val transactionReceipt: String,
    val purchaseToken: String
)

internal enum class PlatformRecordStatus {
    RECORDED,
    NOT_RECORDED
}

internal enum class ProductConsumeStatus {
    CONSUMED,
    NOT_CONSUMED
}

internal data class MiniAppPurchaseRecordCache(
    val miniAppPurchaseRecord: MiniAppPurchaseRecord,
    val platformRecordStatus: PlatformRecordStatus,
    val productConsumeStatus: ProductConsumeStatus,
    val transactionState: TransactionState
)
