package com.rakuten.tech.mobile.miniapp.js.iap

import androidx.annotation.Keep

@Keep
internal data class PurchaseItem(
    val androidStoreId: String,
    val productId: String
)
