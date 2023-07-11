package com.rakuten.tech.mobile.miniapp.analytics

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Mini App Analytics info type.
 */
@Keep
data class MAAnalyticsInfo(
    val eventType: MAAnalyticsEventType,
    val actionType: MAAnalyticsActionType,
    val pageName: String,
    val componentName: String,
    val elementType: String,
    val data: String,
)

/**
 * Action Type.
 */
@Keep
enum class MAAnalyticsActionType {
    @SerializedName("open")
    OPEN,
    @SerializedName("close")
    CLOSE,
    @SerializedName("add")
    ADD,
    @SerializedName("delete")
    DELETE,
    @SerializedName("change")
    CHANGE
}

/**
 * Event Type.
 */
@Keep
enum class MAAnalyticsEventType {
    @SerializedName("appear")
    APPEAR,
    @SerializedName("click")
    CLICK,
    @SerializedName("error")
    ERROR,
    @SerializedName("custom")
    CUSTOM,
}
