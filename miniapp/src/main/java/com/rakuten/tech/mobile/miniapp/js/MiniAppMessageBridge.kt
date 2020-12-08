package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.Intent
import android.webkit.JavascriptInterface
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.CustomPermissionsNotImplementedException
import com.rakuten.tech.mobile.miniapp.ads.AdMobDisplayer
import com.rakuten.tech.mobile.miniapp.ads.MiniAppAdDisplayer
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.CustomPermissionBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionType
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.ui.MiniAppCustomPermissionWindow

@Suppress("TooGenericExceptionCaught", "ComplexMethod", "LargeClass", "TooManyFunctions", "LongMethod")
/** Bridge interface for communicating with mini app. **/
abstract class MiniAppMessageBridge {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private var miniAppViewInitialized = false
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var customPermissionBridgeDispatcher: CustomPermissionBridgeDispatcher
    private lateinit var customPermissionWindow: MiniAppCustomPermissionWindow
    private lateinit var miniAppId: String
    private lateinit var activity: Activity
    private lateinit var userInfoBridgeDispatcher: UserInfoBridgeDispatcher
    private val adBridgeDispatcher = AdBridgeDispatcher()

    private lateinit var screenBridgeDispatcher: ScreenBridgeDispatcher
    private var allowScreenOrientation = false

    internal fun init(
        activity: Activity,
        webViewListener: WebViewListener,
        customPermissionCache: MiniAppCustomPermissionCache,
        miniAppId: String
    ) {
        this.activity = activity
        this.miniAppId = miniAppId
        this.bridgeExecutor = createBridgeExecutor(webViewListener)
        this.customPermissionCache = customPermissionCache
        this.customPermissionBridgeDispatcher = CustomPermissionBridgeDispatcher(
            bridgeExecutor,
            customPermissionCache,
            miniAppId
        )
        this.customPermissionWindow = MiniAppCustomPermissionWindow(
            activity,
            customPermissionBridgeDispatcher
        )

        this.screenBridgeDispatcher = ScreenBridgeDispatcher(activity, bridgeExecutor, allowScreenOrientation)
        adBridgeDispatcher.setBridgeExecutor(bridgeExecutor)

        if (this::userInfoBridgeDispatcher.isInitialized)
            this.userInfoBridgeDispatcher.init(bridgeExecutor, customPermissionCache, miniAppId)
        else this.userInfoBridgeDispatcher = object : UserInfoBridgeDispatcher() {}

        miniAppViewInitialized = true
    }

    @VisibleForTesting
    internal fun createBridgeExecutor(webViewListener: WebViewListener) = MiniAppBridgeExecutor(webViewListener)

    /**
     * Get provided id of mini app for any purpose.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
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
        throw CustomPermissionsNotImplementedException()
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

    @SuppressWarnings("UndocumentedPublicFunction")
    @JavascriptInterface
    fun postMessage(jsonStr: String) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)

        when (callbackObj.action) {
            ActionType.GET_UNIQUE_ID.action -> onGetUniqueId(callbackObj)
            ActionType.REQUEST_PERMISSION.action -> onRequestPermission(callbackObj)
            ActionType.REQUEST_CUSTOM_PERMISSIONS.action -> onRequestCustomPermissions(jsonStr)
            ActionType.SHARE_INFO.action -> onShareContent(callbackObj.id, jsonStr)
            ActionType.LOAD_AD.action -> adBridgeDispatcher.onLoadAd(callbackObj.id, jsonStr)
            ActionType.SHOW_AD.action -> adBridgeDispatcher.onShowAd(callbackObj.id, jsonStr)
            ActionType.GET_USER_NAME.action -> userInfoBridgeDispatcher.onGetUserName(callbackObj.id)
            ActionType.GET_PROFILE_PHOTO.action -> userInfoBridgeDispatcher.onGetProfilePhoto(callbackObj.id)
            ActionType.GET_ACCESS_TOKEN.action -> userInfoBridgeDispatcher.onGetAccessToken(callbackObj.id)
            ActionType.SET_SCREEN_ORIENTATION.action -> screenBridgeDispatcher.onScreenRequest(callbackObj)
            ActionType.GET_CONTACTS.action -> userInfoBridgeDispatcher.onGetContacts(callbackObj.id)
        }
    }

    /** Set implemented ads displayer. Can use the default provided class from sdk [AdMobDisplayer]. **/
    fun setAdMobDisplayer(adDisplayer: MiniAppAdDisplayer) = adBridgeDispatcher.setAdMobDisplayer(adDisplayer)

    /**
     * Set implemented userInfoBridgeDispatcher.
     * Can use the default provided class from sdk [UserInfoBridgeDispatcher].
     **/
    fun setUserInfoBridgeDispatcher(bridgeDispatcher: UserInfoBridgeDispatcher) {
        userInfoBridgeDispatcher = bridgeDispatcher
        if (miniAppViewInitialized)
            userInfoBridgeDispatcher.init(bridgeExecutor, customPermissionCache, miniAppId)
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

    @Suppress("SwallowedException")
    private fun onRequestCustomPermissions(jsonStr: String) {
        // initialize required properties using jsonStr before executing operations
        customPermissionBridgeDispatcher.initCallBackObject(jsonStr)

        // check if there is any denied permission
        val deniedPermissions = customPermissionBridgeDispatcher.filterDeniedPermissions()

        if (deniedPermissions.isNotEmpty()) {
            try {
                requestCustomPermissions(
                    deniedPermissions
                ) { permissionsWithResult ->
                    customPermissionBridgeDispatcher.sendHostAppCustomPermissions(
                        permissionsWithResult
                    )
                }
            } catch (e: CustomPermissionsNotImplementedException) {
                customPermissionWindow.displayPermissions(miniAppId, deniedPermissions)
            } catch (e: Exception) {
                customPermissionBridgeDispatcher.postCustomPermissionError(e.message.toString())
            }
        } else {
            customPermissionBridgeDispatcher.sendCachedCustomPermissions()
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

    internal fun onWebViewDetach() {
        screenBridgeDispatcher.releaseLock()
    }

    /** Allow miniapp to change screen orientation. The default setting is false. */
    fun allowScreenOrientation(isAllowed: Boolean) {
        allowScreenOrientation = isAllowed
        if (this::screenBridgeDispatcher.isInitialized)
            screenBridgeDispatcher.allowScreenOrientation = allowScreenOrientation
    }
}

internal object ErrorBridgeMessage {
    const val ERR_NO_SUPPORT_HOSTAPP = "No support from hostapp"
    const val ERR_UNIQUE_ID = "Cannot get unique id:"
    const val ERR_REQ_PERMISSION = "Cannot request permission:"
    const val ERR_REQ_CUSTOM_PERMISSION = "Cannot request custom permissions:"
    const val NO_IMPLEMENT_CUSTOM_PERMISSION =
        "The `MiniAppMessageBridge.requestCustomPermissions`" +
                " method has not been implemented by the Host App."
    const val ERR_SHARE_CONTENT = "Cannot share content:"
    const val ERR_LOAD_AD = "Cannot load ad:"
    const val ERR_SHOW_AD = "Cannot show ad:"
    const val ERR_SCREEN_ACTION = "Cannot request screen action:"
}
