package com.rakuten.tech.mobile.miniapp.iap

import androidx.annotation.Keep

/**
 * A class to provide the custom errors specific for access token.
 */
@Keep
class MiniAppInAppPurchaseErrorType(val type: String? = null, val message: String? = null) {

    companion object {
        private const val PurchaseFailedError = "PurchaseFailedError"
        private const val ConsumeFailedError = "ConsumeFailedError"
        private const val ProductNotFoundError = "ProductNotFoundError"
        private const val ProductPurchasedAlreadyError = "ProductPurchasedAlreadyError"
        private const val UserCancelledPurchaseError = "UserCancelledPurchaseError"

        // Requested Audience is not supported.
        val purchaseFailedError = MiniAppInAppPurchaseErrorType(type = PurchaseFailedError)

        // Requested Scope is not supported.
        val consumeFailedError = MiniAppInAppPurchaseErrorType(type = ConsumeFailedError)

        // Requested Scope is not supported.
        val productNotFoundError = MiniAppInAppPurchaseErrorType(type = ProductNotFoundError)

        // Requested Scope is not supported.
        val productPurchasedAlreadyError = MiniAppInAppPurchaseErrorType(type = ProductPurchasedAlreadyError)

        // Requested Scope is not supported.
        val userCancelledPurchaseError = MiniAppInAppPurchaseErrorType(type = UserCancelledPurchaseError)

        /**
         *  send custom error message from host app.
         *  @property message error message send to mini app.
         */
        fun unknownError(message: String) = MiniAppInAppPurchaseErrorType(message = message)
    }
}
