package com.rakuten.tech.mobile.miniapp.iap

import androidx.annotation.Keep

/** An object to include in-app purchase product's information. */
@Keep
data class ProductInfo(
    var id: String,
    val title: String,
    val description: String,
    val productPriceInfo: ProductPrice
)

/** An object to include the price information of a [ProductInfo]. */
@Keep

data class ProductPrice(
    val currencyCode: String,
    val price: String
)

/** An object to include the purchased [ProductInfo]. */
@Keep
data class PurchasedProductInfo(
    val productInfo: ProductInfo,
    var transactionId: String,
    var transactionDate: Long
)

/** An object to include the [PurchasedProductInfo] with response status. */
@Keep
data class PurchasedProductResponse(
    val purchaseState: Int,
    val purchasedProductInfo: PurchasedProductInfo,
    val purchaseToken: String,
    val transactionReceipt: String,
)
