package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.Intent
import android.webkit.JavascriptInterface
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionManager
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionType
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionResult

@Suppress("TooGenericExceptionCaught", "SwallowedException", "TooManyFunctions")
/** Bridge interface for communicating with mini app. **/
abstract class MiniAppMessageBridge(val activity: Activity? = null) {
    private lateinit var webViewListener: WebViewListener
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var miniAppInfo: MiniAppInfo

    /** Get provided id of mini app for any purpose. **/
    abstract fun getUniqueId(): String

    /** Post permission request from external. **/
    abstract fun requestPermission(
        miniAppPermissionType: MiniAppPermissionType,
        callback: (isGranted: Boolean) -> Unit
    )

    /**
     * Post custom permissions request.
     * @param permissionsWithDescription list of name and descriptions of custom permissions sent from external.
     * @param callback to invoke a list of name and grant results of custom permissions sent from hostapp.
     */
    abstract fun requestCustomPermissions(
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    )

    /**
     * Share content info [ShareInfo]. This info is provided by mini app.
     * @param content The content property of [ShareInfo] object.
     * @param callback The executed action status should be notified back to mini app.
     **/
    open fun shareContent(
        content: String,
        callback: (isSuccess: Boolean, message: String?) -> Unit
    ) {
        when {
            content.trim().isEmpty() -> callback.invoke(false, "content is empty")
            activity == null -> callback.invoke(false, ErrorBridgeMessage.NO_ACTIVITY)
            else -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, content)
                    type = "text/plain"
                }
                activity.startActivity(Intent.createChooser(sendIntent, null))

                callback.invoke(true, SUCCESS)
            }
        }
    }

    internal fun init(
        webViewListener: WebViewListener,
        customPermissionCache: MiniAppCustomPermissionCache,
        miniAppInfo: MiniAppInfo
    ) {
        this.webViewListener = webViewListener
        this.customPermissionCache = customPermissionCache
        this.miniAppInfo = miniAppInfo
    }

    /** Handle the message from external. **/
    @JavascriptInterface
    fun postMessage(jsonStr: String) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)

        when (callbackObj.action) {
            ActionType.GET_UNIQUE_ID.action -> onGetUniqueId(callbackObj)
            ActionType.REQUEST_PERMISSION.action -> onRequestPermission(callbackObj)
            ActionType.REQUEST_CUSTOM_PERMISSIONS.action -> onRequestCustomPermissions(jsonStr)
            ActionType.SHARE_INFO.action -> onShareContent(callbackObj.id, jsonStr)
        }
    }

    private fun onGetUniqueId(callbackObj: CallbackObj) {
        try {
            postValue(callbackObj.id, getUniqueId())
        } catch (e: Exception) {
            postError(callbackObj.id, "${ErrorBridgeMessage.ERR_UNIQUE_ID} ${e.message}")
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
            postError(callbackObj.id, "${ErrorBridgeMessage.ERR_REQ_PERMISSION} ${e.message}")
        }
    }

    @Suppress("LongMethod")
    private fun onRequestCustomPermissions(jsonStr: String) {
        var callbackObj: CustomPermissionCallbackObj? = null

        try {
            callbackObj = Gson().fromJson(jsonStr, CustomPermissionCallbackObj::class.java)
            val permissionObjList = arrayListOf<CustomPermissionObj>()
            callbackObj.param?.permissions?.forEach {
                permissionObjList.add(CustomPermissionObj(it.name, it.description))
            }

            val permissionsWithDescription = MiniAppCustomPermissionManager()
                .preparePermissionsWithDescription(permissionObjList)

            requestCustomPermissions(
                permissionsWithDescription
            ) { permissionsWithResult ->
                // store values in SDK cache
                val miniAppCustomPermission = MiniAppCustomPermission(
                    miniAppId = miniAppInfo.id,
                    pairValues = permissionsWithResult
                )
                customPermissionCache.storePermissions(miniAppCustomPermission)

                // send JSON response to miniapp
                onRequestCustomPermissionsResult(
                    callbackId = callbackObj.id,
                    jsonResult = MiniAppCustomPermissionManager().createJsonResponse(
                        customPermissionCache,
                        miniAppCustomPermission.miniAppId,
                        permissionsWithDescription
                    )
                )
            }
        } catch (e: Exception) {
            callbackObj?.id?.let {
                postError(
                    it,
                    "${ErrorBridgeMessage.ERR_REQ_CUSTOM_PERMISSION} ${e.message}"
                )
            }
        }
    }

    private fun onShareContent(callbackId: String, jsonStr: String) {
        try {
            val callbackObj = Gson().fromJson(jsonStr, ShareInfoCallbackObj::class.java)

            shareContent(
                callbackObj.param.shareInfo.content
            ) { isSuccess, message ->
                if (isSuccess)
                    postValue(callbackId, message ?: SUCCESS)
                else
                    postError(callbackId,
                        message ?: "${ErrorBridgeMessage.ERR_SHARE_CONTENT} Unknown error message from hostapp.")
            }
        } catch (e: Exception) {
            postError(callbackId, "${ErrorBridgeMessage.ERR_SHARE_CONTENT} ${e.message}")
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

    /** Inform the custom permission request result to MiniApp. **/
    @Suppress("LongMethod", "FunctionMaxLength")
    internal fun onRequestCustomPermissionsResult(callbackId: String, jsonResult: String) {
        postValue(callbackId, jsonResult)
    }

    /** Return a value to mini app. **/
    internal fun postValue(callbackId: String, value: String) {
        webViewListener.runSuccessCallback(callbackId, value)
    }

    /** Emit an error to mini app. **/
    internal fun postError(callbackId: String, errorMessage: String) {
        webViewListener.runErrorCallback(callbackId, errorMessage)
    }
}

internal class ErrorBridgeMessage {

    companion object {
        const val NO_ACTIVITY = "No Activity from MiniAppMessageBridge"
        const val ERR_UNIQUE_ID = "Cannot get unique id:"
        const val ERR_REQ_PERMISSION = "Cannot request permission:"
        const val ERR_REQ_CUSTOM_PERMISSION = "Cannot request custom permissions:"
        const val ERR_SHARE_CONTENT = "Cannot share content:"
    }
}
