package com.rakuten.tech.mobile.miniapp.errors

import androidx.annotation.Keep

/**
 * A data class to prepare the json object of custom error to be sent from this to miniapp.
 */
@Keep
internal data class MiniAppBridgeErrorModel(
    val type: String? = null,
    val message: String? = null
)
