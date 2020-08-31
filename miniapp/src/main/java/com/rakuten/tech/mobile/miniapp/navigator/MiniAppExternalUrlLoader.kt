package com.rakuten.tech.mobile.miniapp.navigator

import android.app.Activity
import android.content.Intent
import com.rakuten.tech.mobile.miniapp.MiniAppScheme

/**
 * This support the scenario that external loader redirect to url which is only supported in mini app view,
 * close the external loader and emit that url to mini app view by [ExternalUrlHandler.emitResult].
 **/
class MiniAppExternalUrlLoader(miniAppId: String, private val activity: Activity) {

    companion object {
        const val returnUrlTag = "return_url_tag"
    }

    private val miniAppScheme = MiniAppScheme(miniAppId)
    private val finishCallBack: (url: String) -> Unit = {
        val returnIntent = Intent().apply { putExtra(returnUrlTag, it) }
        activity.setResult(Activity.RESULT_OK, returnIntent)
        activity.finish()
    }

    /**
     * Determine to close the external loader.
     * Use this in the return value of [WebViewClient.shouldOverrideUrlLoading(WebView, WebResourceRequest)].
     **/
    fun shouldOverrideUrlLoading(url: String): Boolean {
        if (miniAppScheme.isMiniAppUrl(url)) {
            finishCallBack.invoke(url)
            return true
        }

        return false
    }
}
