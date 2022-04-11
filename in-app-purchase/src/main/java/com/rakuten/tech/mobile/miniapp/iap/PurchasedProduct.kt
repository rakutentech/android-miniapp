package com.rakuten.tech.mobile.miniapp.iap

import androidx.annotation.Keep

/** An object to include in-app purchase product's information. */
@Keep
data class Product(
    val id: String,
    val title: String,
    val description: String,
    val price: ProductPrice
)

/** An object to include the price information of a [Product]. */
@Keep
data class ProductPrice(
    val amount: Int,
    val currencyCode: String,
    val price: String
)

/** An object to include the purchased [Product]. */
@Keep
data class PurchasedProduct(
    val orderId: String,
    val product: Product,
    val token: String
)
