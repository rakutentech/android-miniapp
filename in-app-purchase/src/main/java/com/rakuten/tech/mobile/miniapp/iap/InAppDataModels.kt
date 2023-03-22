package com.rakuten.tech.mobile.miniapp.iap

import androidx.annotation.Keep

/**
 * Represents a product.
 * @property id The id of the product.
 * @property title The name of the product.
 * @property description The description of the product.
 * @property productPriceInfo The price info of the product of type [ProductPrice].
 */
@Keep
data class ProductInfo(
    var id: String,
    val title: String,
    val description: String,
    val productPriceInfo: ProductPrice
)

/**
 * Represents price info of a product.
 * @property currencyCode The currency of the price.
 * @property price The price of the product.
 */
@Keep
data class ProductPrice(
    val currencyCode: String,
    val price: String
)

/**
 * Represents a product after purchase.
 * @property productInfo The info of the product purchased of type [ProductInfo].
 * @property transactionId The id of the transaction.
 * @property transactionDate The date of the transaction.
 */
@Keep
data class PurchasedProductInfo(
    val productInfo: ProductInfo,
    var transactionId: String,
    var transactionDate: Long
)

/**
 * Purchase data object for miniapp.
 * @property purchaseState The state of the purchase.
 * @property purchasedProductInfo The product info after purhcase of type [PurchasedProductInfo].
 * @property purchaseToken The token of the purchase.
 * @property transactionReceipt The receipt of the transaction.
 */
@Keep
data class PurchaseData(
    val purchaseState: Int,
    val purchasedProductInfo: PurchasedProductInfo,
    val purchaseToken: String,
    val transactionReceipt: String,
)
