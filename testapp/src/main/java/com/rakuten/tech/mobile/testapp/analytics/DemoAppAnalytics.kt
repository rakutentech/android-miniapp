package com.rakuten.tech.mobile.testapp.analytics

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.analytics.RatTracker
import com.rakuten.tech.mobile.miniapp.BuildConfig
import com.rakuten.tech.mobile.testapp.rat_wrapper.EventType
import com.rakuten.tech.mobile.testapp.rat_wrapper.RATEvent
import org.json.JSONObject

/** Check whether hostapp provides Analytics dependency. */
@Suppress("EmptyCatchBlock", "SwallowedException")
private inline fun <T> whenHasAnalytics(callback: () -> T) {
    try {
        Class.forName("com.rakuten.tech.mobile.analytics.Event")
        callback.invoke()
    } catch (e: ClassNotFoundException) {
    }
}

class DemoAppAnalytics private constructor(private val rasProjectId: String) {

    companion object {
        private var instance: DemoAppAnalytics? = null
        fun init(rasProjectId: String): DemoAppAnalytics {
            return instance ?:  DemoAppAnalytics(rasProjectId).also { instance = it }
        }
    }

    fun sendAnalytics(ratEvent: RATEvent) {
        // Send to this acc/aid
        val params = createParams(
            rasProjectId = rasProjectId,
            acc = BuildConfig.ANALYTICS_ACC,
            aid = BuildConfig.ANALYTICS_AID,
            ratEvent = ratEvent
        )
        trackEvent(ratEvent.getEvent(), params)
    }

    /** common function to create params to send to tracker. */
    @Suppress("LongMethod")
    @VisibleForTesting
    internal fun createParams(
        rasProjectId: String,
        acc: Int,
        aid: Int,
        ratEvent: RATEvent
    ): Map<String, Any> {

        val cp = JSONObject()
            .put("mini_app_project_id", rasProjectId)
            .put("mini_app_sdk_version", BuildConfig.VERSION_NAME)
        if (ratEvent.getMiniAppInfo() != null) {
            cp.put("mini_app_id", ratEvent.getMiniAppInfo()?.id)
                .put("mini_app_version_id", ratEvent.getMiniAppInfo()?.version?.versionId)
        }

        return mapOf<String, Any>(
            "acc" to acc,
            "aid" to aid,
            "actype" to ratEvent.getAction().value,
            "pgn" to ratEvent.getLabel(),
            "ssc" to "demoApp",
            "cp" to cp
        )
    }

    /** common function to send to tracker. */
    private fun trackEvent(eType: EventType, params: Map<String, Any>) = whenHasAnalytics {
        RatTracker.event(eType.value, params).track()
    }
}
