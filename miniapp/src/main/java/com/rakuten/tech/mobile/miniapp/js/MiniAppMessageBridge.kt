package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.Context
import android.webkit.JavascriptInterface
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.display.WebViewListener

@VisibleForTesting
internal const val GET_UNIQUE_ID = "getUniqueId"
@VisibleForTesting
internal const val REQUEST_PERMISSION = "requestPermission"

/** Bridge interface for communicating with mini app. **/
abstract class MiniAppMessageBridge(val context: Context) {
    private lateinit var webViewListener: WebViewListener

    /** Get provided id of mini app for any purpose. **/
    abstract fun getUniqueId(): String

    /** Handle the message from external. **/
    @Suppress("TooGenericExceptionCaught", "SwallowedException", "LongMethod")
    @JavascriptInterface
    fun postMessage(jsonStr: String) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)

        when (callbackObj.action) {
            GET_UNIQUE_ID -> try {
                postValue(callbackObj.id, getUniqueId())
            } catch (e: Exception) {
                postError(callbackObj.id, "Cannot get unique id: ${e.message}")
            }
            REQUEST_PERMISSION -> try {
                val permissionParam = Gson().fromJson(callbackObj.param, Permission::class.java)
                requestPermission(
                    MiniAppPermission.getPermissionRequest(permissionParam.permission),
                    MiniAppPermission.getRequestCode(permissionParam.permission))
            } catch (e: Exception) {
                postError(callbackObj.id, "Cannot request permission: ${e.message}")
            }
        }
    }

    /** Inform the permission request result to MiniApp. **/
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissions.size == grantResults.size) {
            for (i in permissions.indices)
                webViewListener.onRequestPermissionsResult(requestCode, permissions[i], grantResults[i])
        }
    }

    /** Post permission request from external. **/
    internal fun requestPermission(permissions: Array<String>, requestCode: Int) =
        ActivityCompat.requestPermissions(context as Activity, permissions, requestCode)

    /** Return a value to mini app. **/
    internal fun postValue(callbackId: String, value: String) {
        webViewListener.runSuccessCallback(callbackId, value)
    }

    /** Emit an error to mini app. **/
    internal fun postError(callbackId: String, errorMessage: String) {
        webViewListener.runErrorCallback(callbackId, errorMessage)
    }

    internal fun setWebViewListener(webViewListener: WebViewListener) {
        this.webViewListener = webViewListener
    }
}
