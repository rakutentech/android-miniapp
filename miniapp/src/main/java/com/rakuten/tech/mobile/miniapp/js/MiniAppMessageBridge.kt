package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.Intent
import android.webkit.JavascriptInterface
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.ads.AdMobDisplayer
import com.rakuten.tech.mobile.miniapp.ads.MiniAppAdDisplayer
import com.rakuten.tech.mobile.miniapp.ads.isAdMobProvided
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionManager
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionType
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionResult
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoHandler

@Suppress("TooGenericExceptionCaught", "SwallowedException", "TooManyFunctions", "LongMethod",
"LargeClass")
/** Bridge interface for communicating with mini app. **/
abstract class MiniAppMessageBridge {
    private lateinit var bridgeExecutor: BridgeExecutor
    private var miniAppViewInitialized = false
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var miniAppInfo: MiniAppInfo
    private lateinit var activity: Activity
    private lateinit var userInfoHandler: UserInfoHandler
    private lateinit var adDisplayer: MiniAppAdDisplayer
    private var isAdMobEnabled = false

    internal fun init(
        activity: Activity,
        webViewListener: WebViewListener,
        customPermissionCache: MiniAppCustomPermissionCache,
        miniAppInfo: MiniAppInfo
    ) {
        this.activity = activity
        this.bridgeExecutor = createBridgeExecutor(webViewListener)
        this.customPermissionCache = customPermissionCache
        this.miniAppInfo = miniAppInfo

        if (this::userInfoHandler.isInitialized)
            this.userInfoHandler.init(bridgeExecutor, customPermissionCache, miniAppInfo.id)

        miniAppViewInitialized = true
    }

    @VisibleForTesting
    internal fun createBridgeExecutor(webViewListener: WebViewListener) = BridgeExecutor(webViewListener)

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
    open fun requestCustomPermissions(
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        throw MiniAppSdkException(
            "The `MiniAppMessageBridge.requestCustomPermissions`" +
                    " method has not been implemented by the Host App."
        )
    }

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

    /** Handle the message from external. **/
    @JavascriptInterface
    fun postMessage(jsonStr: String) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)

        when (callbackObj.action) {
            ActionType.GET_UNIQUE_ID.action -> onGetUniqueId(callbackObj)
            ActionType.REQUEST_PERMISSION.action -> onRequestPermission(callbackObj)
            ActionType.REQUEST_CUSTOM_PERMISSIONS.action -> onRequestCustomPermissions(jsonStr)
            ActionType.SHARE_INFO.action -> onShareContent(callbackObj.id, jsonStr)
            ActionType.LOAD_AD.action -> onLoadAd(callbackObj.id, jsonStr)
            ActionType.SHOW_AD.action -> onShowAd(callbackObj.id, jsonStr)
            ActionType.GET_USER_NAME.action -> userInfoHandler.onGetUserName(callbackObj.id)
            ActionType.GET_PROFILE_PHOTO.action -> userInfoHandler.onGetProfilePhoto(callbackObj.id)
        }
    }

    /** Set implemented ads displayer. Can use the default provided class from sdk [AdMobDisplayer]. **/
    fun setAdMobDisplayer(adDisplayer: MiniAppAdDisplayer) {
        this.adDisplayer = adDisplayer
        this.isAdMobEnabled = isAdMobProvided()
    }

    /** Set implemented userInfoHandler. Can use the default provided class from sdk [UserInfoHandler]. **/
    fun setUserInfoHandler(handler: UserInfoHandler) {
        userInfoHandler = handler
        if (miniAppViewInitialized)
            userInfoHandler.init(bridgeExecutor, customPermissionCache, miniAppInfo.id)
    }

    private fun onGetUniqueId(callbackObj: CallbackObj) {
        try {
            bridgeExecutor.postValue(callbackObj.id, getUniqueId())
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackObj.id, "${ErrorBridgeMessage.ERR_UNIQUE_ID} ${e.message}")
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
            bridgeExecutor.postError(callbackObj.id, "${ErrorBridgeMessage.ERR_REQ_PERMISSION} ${e.message}")
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
                bridgeExecutor.postError(
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
                    bridgeExecutor.postValue(callbackId, message ?: SUCCESS)
                else
                    bridgeExecutor.postError(callbackId,
                        message ?: "${ErrorBridgeMessage.ERR_SHARE_CONTENT} Unknown error message from hostapp.")
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "${ErrorBridgeMessage.ERR_SHARE_CONTENT} ${e.message}")
        }
    }

    @VisibleForTesting
    /** Inform the permission request result to MiniApp. **/
    internal fun onRequestPermissionsResult(callbackId: String, isGranted: Boolean) {
        if (isGranted)
            bridgeExecutor.postValue(callbackId, MiniAppPermissionResult.getValue(isGranted).type)
        else
            bridgeExecutor.postError(callbackId, MiniAppPermissionResult.getValue(isGranted).type)
    }

    /** Inform the custom permission request result to MiniApp. **/
    @Suppress("LongMethod", "FunctionMaxLength")
    @VisibleForTesting
    internal fun onRequestCustomPermissionsResult(callbackId: String, jsonResult: String) {
        bridgeExecutor.postValue(callbackId, jsonResult)
    }

    private fun onLoadAd(callbackId: String, jsonStr: String) {
        if (isAdMobEnabled) {
            try {
                val callbackObj = Gson().fromJson(jsonStr, AdCallbackObj::class.java)
                val adObj = callbackObj.param

                when (adObj.adType) {
                    AdType.INTERSTITIAL.value -> adDisplayer.loadInterstitial(
                        adUnitId = adObj.adUnitId,
                        onLoaded = { bridgeExecutor.postValue(callbackId, SUCCESS) },
                        onFailed = { errMsg ->
                            bridgeExecutor.postError(
                                callbackId,
                                "${ErrorBridgeMessage.ERR_LOAD_AD} $errMsg"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                bridgeExecutor.postError(callbackId, "${ErrorBridgeMessage.ERR_LOAD_AD} ${e.message}")
            }
        } else
            bridgeExecutor.postError(callbackId, "${ErrorBridgeMessage.ERR_LOAD_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}")
    }

    private fun onShowAd(callbackId: String, jsonStr: String) {
        if (isAdMobEnabled) {
            try {
                val callbackObj = Gson().fromJson(jsonStr, AdCallbackObj::class.java)
                val adObj = callbackObj.param

                when (adObj.adType) {
                    AdType.INTERSTITIAL.value -> adDisplayer.showInterstitial(
                        adUnitId = adObj.adUnitId,
                        onClosed = { bridgeExecutor.postValue(callbackId, CLOSED) },
                        onFailed = { errMsg ->
                            bridgeExecutor.postError(
                                callbackId,
                                "${ErrorBridgeMessage.ERR_SHOW_AD} $errMsg"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                bridgeExecutor.postError(callbackId, "${ErrorBridgeMessage.ERR_SHOW_AD} ${e.message}")
            }
        } else
            bridgeExecutor.postError(callbackId, "${ErrorBridgeMessage.ERR_SHOW_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}")
    }
}

internal class ErrorBridgeMessage {

    companion object {
        const val ERR_NO_SUPPORT_HOSTAPP = "No support from hostapp"
        const val ERR_UNIQUE_ID = "Cannot get unique id:"
        const val ERR_REQ_PERMISSION = "Cannot request permission:"
        const val ERR_REQ_CUSTOM_PERMISSION = "Cannot request custom permissions:"
        const val ERR_SHARE_CONTENT = "Cannot share content:"
        const val ERR_LOAD_AD = "Cannot load ad:"
        const val ERR_SHOW_AD = "Cannot show ad:"
    }
}
