package com.rakuten.tech.mobile.miniapp.js

import android.webkit.JavascriptInterface
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.display.WebViewListener

@Suppress("TooGenericExceptionCaught", "SwallowedException")
/** Bridge interface for communicating with mini app. **/
abstract class MiniAppMessageBridge {
    private lateinit var webViewListener: WebViewListener

    /** Get provided id of mini app for any purpose. **/
    abstract fun getUniqueId(): String

    /** Post permission request from external. **/
    abstract fun requestPermission(
        miniAppPermissionType: MiniAppPermissionType,
        callback: (isGranted: Boolean) -> Unit
    )

    /** Post custom permissions request from external. **/
    abstract fun requestCustomPermissions(
        permissions: List<String>,
        callback: (grantResult: String) -> Unit
    )

    /** Handle the message from external. **/
    @JavascriptInterface
    fun postMessage(jsonStr: String) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)

        when (callbackObj.action) {
            ActionType.GET_UNIQUE_ID.action -> onGetUniqueId(callbackObj)
            ActionType.REQUEST_PERMISSION.action -> onRequestPermission(callbackObj)
            ActionType.REQUEST_CUSTOM_PERMISSIONS.action -> onRequestCustomPermissions(callbackObj)
        }
    }

    private fun onGetUniqueId(callbackObj: CallbackObj) {
        try {
            postValue(callbackObj.id, getUniqueId())
        } catch (e: Exception) {
            postError(callbackObj.id, "Cannot get unique id: ${e.message}")
        }
    }

    private fun onRequestPermission(callbackObj: CallbackObj) {
        try {
            val permissionParam = Gson().fromJson<Permission>(
                callbackObj.param.toString(),
                object : TypeToken<Permission>() {}.type
            )

            requestPermission(
                MiniAppPermissionType.getValue(permissionParam.permission)
            ) { isGranted -> onRequestPermissionsResult(
                callbackId = callbackObj.id,
                isGranted = isGranted
            ) }
        } catch (e: Exception) {
            postError(callbackObj.id, "Cannot request permission: ${e.message}")
        }
    }

    private fun onRequestCustomPermissions(callbackObj: CallbackObj) {
        try {
            val customPermissionParam = Gson().fromJson<CustomPermission>(
                callbackObj.param.toString(),
                object : TypeToken<CustomPermission>() {}.type
            )

            requestCustomPermissions(
                customPermissionParam.permissions
            ) { grantResult -> onRequestCustomPermissionsResult(
                callbackId = callbackObj.id,
                grantResult = grantResult
            ) }
        } catch (e: Exception) {
            postError(callbackObj.id, "Cannot request custom permissions: ${e.message}")
        }
    }

    @VisibleForTesting
    /** Inform the permission request result to MiniApp. **/
    internal fun onRequestPermissionsResult(callbackId: String, isGranted: Boolean) {
        if (isGranted)
            postValue(callbackId, MiniAppPermissionResult.getValue(isGranted).type)
        else
            postError(callbackId, MiniAppPermissionResult.getValue(isGranted).type)
    }

    /** Inform the permission request result to MiniApp. **/
    internal fun onRequestCustomPermissionsResult(callbackId: String, grantResult: String) {
        postValue(callbackId, grantResult)
    }

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
