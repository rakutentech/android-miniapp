package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient

/**
 * The file picker of a miniapp which needs to be implemented by the HostApp.
 * @param requestCode of file picking using an intent inside sdk, which will also be used
 * to retrieve the data by [Activity.onActivityResult] in the HostApp.
 **/
open class MiniAppFilePicker(var requestCode: Int) {

    internal var callback: ValueCallback<Array<Uri>>? = null

    /**
     * HostApp can override [WebChromeClient.onShowFileChooser] to choose a file
     * which is requested using HTML forms with 'file' input type.
     * @param filePathCallback The callback to receive the Uris.
     * @param fileChooserParams The file chooser parameters can be used to create an intent to choose file.
     * @param context Activity's context from the HostApp.
     */
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
        context: Context
    ): Boolean {
        try {
            this.callback = filePathCallback
            val intent = fileChooserParams?.createIntent()
            (context as Activity).startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            Log.e(MiniAppFilePicker::class.java.simpleName, e.message.toString())
            return false
        }
        return true
    }

    /**
     * Receive the files from the HostApp.
     * @param files The array of Uri to be invoked by a filePathCallback after successfully retrieved
     * by [Activity.onActivityResult] in the HostApp.
     */
    fun onReceivedFiles(files: Array<Uri>) {
        callback?.onReceiveValue(files)
    }
}
