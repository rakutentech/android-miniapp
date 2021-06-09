package com.rakuten.tech.mobile.miniapp.analytics

import androidx.annotation.Keep

/**
 *  Contains the components which need to add extra analytics credentials from host app.
 *  @property acc The RAT account id.
 *  @property aid The RAT app id.
 */
@Keep
data class MiniAppAnalyticsConfig(
    val acc: Int,
    val aid: Int
)
