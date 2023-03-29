package com.rakuten.tech.mobile.miniapp.js.iap

import androidx.annotation.Keep

@Keep
internal data class MiniAppPurchaseItemListResponse(
    val items: List<PurchaseItem>
)
