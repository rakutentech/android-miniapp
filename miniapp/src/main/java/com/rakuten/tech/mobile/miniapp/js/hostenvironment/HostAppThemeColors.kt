package com.rakuten.tech.mobile.miniapp.js.hostenvironment

import androidx.annotation.Keep

/**
 * Represents the color theme of the host app.
 * @property primaryColor The primary color of the host app.
 * @property secondaryColor The secondary color of the host app.
 */
@Keep
data class HostAppThemeColors(
    var primaryColor: String?,
    val secondaryColor: String?
)
