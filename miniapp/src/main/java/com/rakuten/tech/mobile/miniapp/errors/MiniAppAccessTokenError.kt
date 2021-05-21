package com.rakuten.tech.mobile.miniapp.errors

import androidx.annotation.Keep

/**
 *  Contains the components to use custom errors from host app.
 *  @property error The type of accessToken error.
 *  @property message error message send to min app.
 */
@Keep
data class MiniAppAccessTokenError(
    val error: AccessTokenErrorType,
    val message: String = ""
) {
    constructor() : this(error = AccessTokenErrorType.Error, message = "")
}
