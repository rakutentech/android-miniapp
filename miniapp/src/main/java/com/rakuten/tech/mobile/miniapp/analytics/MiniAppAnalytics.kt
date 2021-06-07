package com.rakuten.tech.mobile.miniapp.analytics

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.analytics.RatTracker
import com.rakuten.tech.mobile.miniapp.BuildConfig
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import org.json.JSONObject

/** Check whether hostapp provides Analytics dependency. */
@Suppress("EmptyCatchBlock", "SwallowedException")
private inline fun <T> whenHasAnalytics(callback: () -> T) {
    try {
        Class.forName("com.rakuten.tech.mobile.analytics.Event")
        callback.invoke()
    } catch (e: ClassNotFoundException) {}
}

/** Only init when analytics dependency is provided. */
@Suppress("SwallowedException", "TooGenericExceptionCaught")
internal class MiniAppAnalytics(
    val rasProjectId: String,
    private val configs: List<MiniAppAnalyticsConfig>
) {

    companion object {
        @Suppress("LongMethod")
        internal fun sendAnalyticsDefault(
            rasProjectId: String,
            eType: Etype,
            actype: Actype,
            miniAppInfo: MiniAppInfo?
        ) = try {
            val params = createParams(
                rasProjectId = rasProjectId,
                acc = BuildConfig.ANALYTICS_ACC,
                aid = BuildConfig.ANALYTICS_AID,
                actype = actype,
                miniAppInfo = miniAppInfo
            )
            whenHasAnalytics {
                RatTracker.event(eType.value, params).track()
            }
        } catch (e: Exception) {
            Log.e("MiniAppAnalytics", e.message.orEmpty())
        }

        /** common function to create params to send to tracker. */
        @Suppress("LongMethod")
        @VisibleForTesting
        internal fun createParams(
            rasProjectId: String,
            acc: Int,
            aid: Int,
            actype: Actype,
            miniAppInfo: MiniAppInfo?
        ): MutableMap<String, Any> {
            val params = mutableMapOf<String, Any>()
            params["acc"] = acc
            params["aid"] = aid
            params["actype"] = actype.value

            val cp = JSONObject()
                .put("mini_app_project_id", rasProjectId)
                .put("mini_app_sdk_version", BuildConfig.VERSION_NAME)
            if (miniAppInfo != null) {
                cp.put("mini_app_id", miniAppInfo.id)
                    .put("mini_app_version_id", miniAppInfo.version.versionId)
            }
            params["cp"] = cp

            return params
        }
    }

    @Suppress("LongMethod")
    internal fun sendAnalytics(eType: Etype, actype: Actype, miniAppInfo: MiniAppInfo?) = try {
        // Send to this acc/aid
        val params = createParams(
            rasProjectId = rasProjectId,
            acc = BuildConfig.ANALYTICS_ACC,
            aid = BuildConfig.ANALYTICS_AID,
            actype = actype,
            miniAppInfo = miniAppInfo
        )
        whenHasAnalytics {
            RatTracker.event(eType.value, params).track()
        }
        // Send to all the external acc/aid added by host app
        for ((acc, aid) in configs) {
            val params = createParams(
                rasProjectId = rasProjectId,
                acc = acc,
                aid = aid,
                actype = actype,
                miniAppInfo = miniAppInfo
            )
            whenHasAnalytics {
                RatTracker.event(eType.value, params).track()
            }
        }
    } catch (e: Exception) {
        Log.e("MiniAppAnalytics", e.message.orEmpty())
    }
}
