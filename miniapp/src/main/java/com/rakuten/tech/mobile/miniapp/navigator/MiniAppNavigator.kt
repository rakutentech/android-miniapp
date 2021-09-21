package com.rakuten.tech.mobile.miniapp.navigator

import android.webkit.WebView
import com.rakuten.tech.mobile.miniapp.MiniApp

/**
 * The navigation controller of sdk mini app view.
 * You can optionally pass an implementation of this when creating a mini app using [MiniApp.create]
 */
interface MiniAppNavigator {

    /**
     * Open the external url by browser or webview.
     * @param url The detected external url. This url is sent from mini app view.
     * @param externalResultHandler Use this to send any result such as url to mini app view.
     */
    fun openExternalUrl(url: String, externalResultHandler: ExternalResultHandler)
}

/**
 * File download controller for mini app view.
 * This interface can optionally be used with your MiniAppNavigator if you wish to intercept
 * file download requests from the mini app. If you do not use this interface,
 * then default handling will be used for file download requests.
 */
interface MiniAppDownloadNavigator : MiniAppNavigator {
    /**
     * Notify the host application that a file should be downloaded.
     *
     * Note that this will only receive requests When the mini app downloaded a file via JavaScript XHR.
     * In this case the [url] received here will be a base64 data string which you must decode to bytes.
     *
     * In the case that a mini app wants to download a file from an external URL
     * such as https://www.example.com/test.zip, the request will be sent to [MiniAppNavigator.openExternalUrl],
     * so you should handle this case in your custom WebView instance using [WebView.setDownloadListener].
     *
     * @param url The full url to the content that should be downloaded
     * @param userAgent the user agent to be used for the download.
     * @param contentDisposition Content-disposition http header, if
     *                           present.
     * @param mimetype The mimetype of the content reported by the server
     * @param contentLength The file size reported by the server
     */
    fun onFileDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String,
        contentLength: Long
    )
}
