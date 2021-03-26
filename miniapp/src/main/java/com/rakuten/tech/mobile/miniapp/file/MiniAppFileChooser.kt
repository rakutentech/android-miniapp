package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient

/**
 * The file chooser of a miniapp with `onShowFileChooser` function.
 **/
interface MiniAppFileChooser {

    /**
     * The file chooser of a miniapp with `onShowFileChooser` function.
     * @param filePathCallback a callback to provide the array of file-paths to select.
     * @param fileChooserParams the parameters can be used to customize the options of file chooser.
     * @param context the Activity context can be used to start the intent to choose file.
     **/
    fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
        context: Context
    ): Boolean
}

/**
 * The default file chooser of a miniapp.
 * @param requestCode of file choosing using an intent inside sdk, which will also be used
 * to retrieve the data by [Activity.onActivityResult] in the HostApp.
 **/
class MiniAppFileChooserDefault(var requestCode: Int) : MiniAppFileChooser {

    internal var callback: ValueCallback<Array<Uri>>? = null

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
        context: Context
    ): Boolean {
        try {
            this.callback = filePathCallback
            val intent = fileChooserParams?.createIntent()
            (context as Activity).startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            Log.e(MiniAppFileChooser::class.java.simpleName, e.message.toString())
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
