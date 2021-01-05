package com.rakuten.tech.mobile.miniapp.analytics

import com.rakuten.tech.mobile.analytics.RatTracker
import com.rakuten.tech.mobile.miniapp.BuildConfig
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import org.json.JSONObject

/** Check whether hostapp provides Analytics dependency. */
@Suppress("EmptyCatchBlock", "SwallowedException")
private inline fun <T> whenHasAnalytics(callback: () -> T) {
    try {
        Class.forName("com.rakuten.tech.mobile.analytics")
        callback.invoke()
    } catch (e: ClassNotFoundException) {}
}

/** Only init when analytics dependency is provided. */
internal class MiniAppAnalytics(val rasProjectId: String) {

    companion object {
        var instance: MiniAppAnalytics? = null

        fun init(rasProjectId: String) = whenHasAnalytics {
            instance = MiniAppAnalytics(rasProjectId)
        }
    }

    fun sendAnalytics(eType: Etype, actype: Actype, miniAppInfo: MiniAppInfo?) {
        val params = mutableMapOf<String, Any>()
        // Send to this acc/aid
        params["acc"] = BuildConfig.ANALYTICS_ACC
        params["aid"] = BuildConfig.ANALYTICS_AID

        params["actype"] = actype.value
        if (miniAppInfo != null) {
            params["cp"] = JSONObject()
                .put("mini_app_project_id", rasProjectId)
                .put("mini_app_id", miniAppInfo.id)
                .put("mini_app_version_id", miniAppInfo.version.versionId)
        }

        RatTracker.event("rat.${eType.value}", params).track()
    }
}
