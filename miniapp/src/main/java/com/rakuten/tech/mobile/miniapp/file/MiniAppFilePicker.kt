package com.rakuten.tech.mobile.miniapp.file

import android.net.Uri
import android.webkit.ValueCallback

/** The file picker of a miniapp. **/
interface MiniAppFilePicker {

    /**
     * Request to pick a file from the device within the miniapp webview per [requestCode].
     * @param filePathCallback a file path callback to pass to HostApp has been found
     * by [MiniAppWebChromeClient.onShowFileChooser].
     * @param callback to pass the request code of file picking using an intent inside sdk,
     * which will also be used to retrieve the data using onActivityResult in the host app.
     */
    fun requestFile(
        filePathCallback: ValueCallback<Array<Uri>>?,
        callback: (requestCode: Int) -> Unit
    )
}
