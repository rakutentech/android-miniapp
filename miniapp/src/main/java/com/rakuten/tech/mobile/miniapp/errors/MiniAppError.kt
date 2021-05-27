package com.rakuten.tech.mobile.miniapp.errors

import androidx.annotation.Keep

/**
 *  Contains the components to use custom errors from host app.
 *  @property type The type of error.
 *  @property message error message send to min app.
 */
@Keep
data class MiniAppError(
    val type: String = "",
    val message: String = ""
) {
    constructor() : this(type = "", message = "")
}
