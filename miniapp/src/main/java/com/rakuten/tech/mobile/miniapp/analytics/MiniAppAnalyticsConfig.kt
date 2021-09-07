package com.rakuten.tech.mobile.miniapp.analytics

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 *  Contains the components which need to add extra analytics credentials from host app.
 *  @property acc The RAT account id.
 *  @property aid The RAT app id.
 */
@Keep
@Parcelize
data class MiniAppAnalyticsConfig(
    val acc: Int,
    val aid: Int
): Parcelable
