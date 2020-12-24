package com.rakuten.tech.mobile.miniapp.analytics

import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import org.json.JSONObject

/**
 * Check whether hostapp provides Analytics dependency.
 * The class name can be replaced with analytics package for real tracking.
 */
@Suppress("EmptyCatchBlock", "SwallowedException")
private inline fun <T> whenHasAnalytics(callback: () -> T) {
    try {
        // ToDo replace with analytics package.
        Class.forName("com.rakuten.tech.mobile.miniapp.analytics.Event")
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
        params["actype"] = actype.value
        if (miniAppInfo != null) {
            params["cp"] = JSONObject()
                .put("mini_app_project_id", rasProjectId)
                .put("mini_app_id", miniAppInfo.id)
                .put("mini_app_version_id", miniAppInfo.version.versionId)
        }

        // ToDo replace the dummy Event.
        Event("rat.${eType.value}", params).track()
    }
}
