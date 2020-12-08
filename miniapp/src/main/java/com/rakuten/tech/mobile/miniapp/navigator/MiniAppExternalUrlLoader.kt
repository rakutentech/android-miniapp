package com.rakuten.tech.mobile.miniapp.navigator

import android.app.Activity
import android.content.Intent
import com.rakuten.tech.mobile.miniapp.MiniAppScheme

/**
 * This support the scenario that external loader redirect to url which is only supported in mini app view,
 * close the external loader and emit that url to mini app view by [ExternalResultHandler.emitResult].
 **/
class MiniAppExternalUrlLoader private constructor(
    private val activity: Activity?
) {

    companion object {
        const val returnUrlTag = "return_url_tag"

        /**
         * Creates new MiniAppExternalUrlLoader.
         * @param miniAppId The id of loading mini app.
         * @param activity The Activity contains webview. Pass the activity if you want to auto finish
         * the Activity with current external loading url as result data.
         **/
        fun loaderWithId(miniAppId: String, activity: Activity? = null): MiniAppExternalUrlLoader {
            val loader = MiniAppExternalUrlLoader(activity)
            loader.miniAppId = miniAppId
            return loader
        }

        /**
         * Creates new MiniAppExternalUrlLoader.
         * This should only be used for previewing a mini app from a local server.
         * @param customAppUrl The url that was used to load the Mini App.
         * @param activity The Activity contains webview. Pass the activity if you want to auto finish
         * the Activity with current external loading url as result data.
         **/
        fun loaderWithUrl(customAppUrl: String, activity: Activity? = null): MiniAppExternalUrlLoader {
            val loader = MiniAppExternalUrlLoader(activity)
            loader.customAppUrl = customAppUrl
            return loader
        }
    }

    private var miniAppId = ""
    private var customAppUrl: String? = null
    private val miniAppScheme: MiniAppScheme by lazy {
        if (customAppUrl != null) {
            MiniAppScheme.schemeWithCustomUrl(customAppUrl!!)
        } else {
            MiniAppScheme.schemeWithAppId(miniAppId)
        }
    }

    /**
     * Determine to close the external loader.
     * Use this in the return value of [WebViewClient.shouldOverrideUrlLoading(WebView, WebResourceRequest)].
     **/
    fun shouldOverrideUrlLoading(url: String): Boolean {
        var shouldCancelLoading = false
        if (url.startsWith("tel:") && activity != null) {
            miniAppScheme.openPhoneDialer(activity, url)
            shouldCancelLoading = true
        } else if (shouldClose(url)) {
            closeExternalView(url)
            shouldCancelLoading = true
        }

        return shouldCancelLoading
    }

    /**
     * In case you do not want to finish activity which contains webview automatically, use this to
     * check should stop the external webview loader and send the current url to mini app view.
     */
    fun shouldClose(url: String): Boolean = miniAppScheme.isMiniAppUrl(url)

    private fun closeExternalView(url: String) = activity?.run {
        val returnIntent = Intent().apply { putExtra(returnUrlTag, url) }
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
