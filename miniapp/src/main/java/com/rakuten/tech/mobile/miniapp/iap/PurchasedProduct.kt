package com.rakuten.tech.mobile.miniapp.iap

import androidx.annotation.Keep

/** Product object for miniapp. */
@Keep
data class Product(
    val id: String,
    val title: String,
    val description: String,
    val price: ProductPrice
)

/** ProductPrice object for miniapp. */
@Keep
data class ProductPrice(
    val amount: Int,
    val currencyCode: String,
    val price: String
)

/** PurchasedProduct object for miniapp. */
@Keep
data class PurchasedProduct(
    val orderId: String,
    val product: Product,
    val token: String
)
