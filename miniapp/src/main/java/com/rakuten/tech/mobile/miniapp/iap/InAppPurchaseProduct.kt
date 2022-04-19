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
    val currencyCode: String,
    val price: String
)

/** An object to include the purchased [Product]. */
@Keep
data class PurchasedProduct(
    val product: Product,
    val transactionId: String,
    val transactionDate: String
)

/** An object to include the [PurchasedProduct] with response status. */
@Keep
data class PurchasedProductResponse(
    val status: PurchasedProductResponseStatus,
    val purchasedProduct: PurchasedProduct
)

/** Status of Purchased Product Response. **/
enum class PurchasedProductResponseStatus(val type: String) {
    PURCHASED("PURCHASED"),
    FAILED("FAILED"),
    RESTORED("RESTORED"),
    UNKNOWN("UNKNOWN");

    internal companion object {
        internal fun getValue(type: String) = values().find { it.type == type } ?: UNKNOWN
    }
}
