package com.rakuten.tech.mobile.miniapp.iap

import androidx.annotation.Keep

/**
 * A class to provide the custom errors specific for in app purchase.
 */
@Keep
class MiniAppInAppPurchaseErrorType(val type: String? = null, val message: String? = null) {

    companion object {
        private const val PurchaseFailedError = "PurchaseFailedError"
        private const val ConsumeFailedError = "ConsumeFailedError"
        private const val ProductNotFoundError = "ProductNotFoundError"
        private const val ProductPurchasedAlreadyError = "ProductPurchasedAlreadyError"
        private const val UserCancelledPurchaseError = "UserCancelledPurchaseError"

        // Requested Purchase failed.
        val purchaseFailedError = MiniAppInAppPurchaseErrorType(type = PurchaseFailedError)

        // Requested Consume failed.
        val consumeFailedError = MiniAppInAppPurchaseErrorType(type = ConsumeFailedError)

        // Requested Product Purchase/Consume not found.
        val productNotFoundError = MiniAppInAppPurchaseErrorType(type = ProductNotFoundError)

        // Requested Product already owned/purchased.
        val productPurchasedAlreadyError = MiniAppInAppPurchaseErrorType(type = ProductPurchasedAlreadyError)

        // Requested Purchase cancelled.
        val userCancelledPurchaseError = MiniAppInAppPurchaseErrorType(type = UserCancelledPurchaseError)

        /**
         *  send custom error message from host app.
         *  @property message error message send to mini app.
         */
        fun unknownError(message: String) = MiniAppInAppPurchaseErrorType(message = message)
    }
}
