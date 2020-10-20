package com.rakuten.tech.mobile.miniapp.navigator

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.core.net.toUri
import com.rakuten.tech.mobile.miniapp.MiniAppScheme

/**
 * This support the scenario that external loader redirect to url which is only supported in mini app view,
 * close the external loader and emit that url to mini app view by [ExternalResultHandler.emitResult].
 * @param miniAppId The id of loading mini app.
 * @param activity The Activity contains webview. Pass the activity if you want to auto finish
 * the Activity with current external loading url as result data.
 **/
class MiniAppExternalUrlLoader(miniAppId: String, private val activity: Activity? = null) {

    companion object {
        const val returnUrlTag = "return_url_tag"
    }

    private val miniAppScheme = MiniAppScheme(miniAppId)

    /**
     * Determine to close the external loader.
     * Use this in the return value of [WebViewClient.shouldOverrideUrlLoading(WebView, WebResourceRequest)].
     **/
    fun shouldOverrideUrlLoading(url: String): Boolean {
        var shouldCancelLoading = false
        if (shouldClose(url)) {
            closeExternalView(url)
            shouldCancelLoading = true
        } else if (activity != null) {
            if (url.startsWith("tel:")) {
                miniAppScheme.openPhoneDialer(activity, url)
                shouldCancelLoading = true
            } else if (url.startsWith("http://")) {
                openNonSSLDialog(url)
                shouldCancelLoading = true
            }
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

    private fun openNonSSLDialog(url: String) =
        AlertDialog.Builder(activity)
            .setMessage("This link is unsafe. If you would like to proceed, it will be opened in your native browser.")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                activity?.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
}
