package com.rakuten.tech.mobile.miniapp.analytics

import androidx.annotation.Keep

/**
 * Mini App Analytics info type
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
 * Action Type
 */
@Keep
enum class MAAnalyticsActionType(val type: String) {
    OPEN("open"),
    CLOSE("close"),
    ADD("add"),
    DELETE("delete"),
    CHANGE("change")
}

/**
 * Event Type
 */
@Keep
enum class MAAnalyticsEventType(val type: String) {
    APPEAR("appear"),
    CLICK("click"),
    ERROR("error"),
    CUSTOM("custom"),
}
