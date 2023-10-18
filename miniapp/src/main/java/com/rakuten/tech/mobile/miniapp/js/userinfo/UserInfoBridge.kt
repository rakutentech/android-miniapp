package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.errors.MiniAppBridgeErrorModel
import com.rakuten.tech.mobile.miniapp.errors.MiniAppPointsError
import com.rakuten.tech.mobile.miniapp.js.AccessTokenCallbackObj
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.js.base64Encoded
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import java.util.ArrayList

@Suppress("LargeClass", "TooGenericExceptionCaught", "TooManyFunctions")
internal class UserInfoBridge {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var downloadedManifestCache: DownloadedManifestCache
    private lateinit var miniAppId: String
    private var isMiniAppComponentReady = false
    private lateinit var userInfoBridgeDispatcher: UserInfoBridgeDispatcher

    fun setMiniAppComponents(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        downloadedManifestCache: DownloadedManifestCache,
        miniAppId: String
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.customPermissionCache = miniAppCustomPermissionCache
        this.downloadedManifestCache = downloadedManifestCache
        this.miniAppId = miniAppId
        isMiniAppComponentReady = true
    }

    fun setUserInfoBridgeDispatcher(userInfoBridgeDispatcher: UserInfoBridgeDispatcher) {
        this.userInfoBridgeDispatcher = userInfoBridgeDispatcher
    }

    private fun <T> whenReady(callbackId: String, callback: () -> T) {
        if (isMiniAppComponentReady) {
            if (this::userInfoBridgeDispatcher.isInitialized)
                callback.invoke()
            else
                bridgeExecutor.postError(callbackId, NO_IMPL)
        }
    }

    fun onGetUserName(callbackId: String) = whenReady(callbackId) {
        try {
            if (customPermissionCache.hasPermission(miniAppId, MiniAppCustomPermissionType.USER_NAME)) {
                // asynchronously retrieve user name
                val successCallback = { userName: String ->
                    bridgeExecutor.postValue(
                        callbackId,
                        userName.base64Encoded()
                    )
                }
                val errorCallback = { message: String ->
                    bridgeExecutor.postError(callbackId, "$ERR_GET_USER_NAME $message")
                }

                userInfoBridgeDispatcher.getUserName(successCallback, errorCallback)
            } else
                bridgeExecutor.postError(callbackId, "$ERR_GET_USER_NAME $ERR_USER_NAME_NO_PERMISSION")
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_GET_USER_NAME ${e.message}")
        }
    }

    fun onGetProfilePhoto(callbackId: String) = whenReady(callbackId) {
        try {
            if (customPermissionCache.hasPermission(miniAppId, MiniAppCustomPermissionType.PROFILE_PHOTO)) {

                // asynchronously retrieve profile photo url
                val successCallback = { photoUrl: String -> bridgeExecutor.postValue(callbackId, photoUrl) }
                val errorCallback = { message: String ->
                    bridgeExecutor.postError(callbackId, "$ERR_GET_PROFILE_PHOTO $message")
                }

                userInfoBridgeDispatcher.getProfilePhoto(successCallback, errorCallback)
            } else
                bridgeExecutor.postError(callbackId, "$ERR_GET_PROFILE_PHOTO $ERR_PROFILE_PHOTO_NO_PERMISSION")
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_GET_PROFILE_PHOTO ${e.message}")
        }
    }

    @Suppress("LongMethod")
    internal fun onGetAccessToken(callbackId: String, jsonStr: String) = whenReady(callbackId) {
        try {
            val callbackObj: AccessTokenCallbackObj = Gson().fromJson(jsonStr, AccessTokenCallbackObj::class.java)
            if (customPermissionCache.hasPermission(miniAppId, MiniAppCustomPermissionType.ACCESS_TOKEN)) {
                onHasAccessTokenPermission(callbackObj)
            } else
                bridgeExecutor.postError(
                    callbackObj.id,
                    Gson().toJson(
                        MiniAppBridgeErrorModel(
                            message = "$ERR_GET_ACCESS_TOKEN $ERR_ACCESS_TOKEN_NO_PERMISSION"
                        )
                    )
                )
        } catch (e: Exception) {
            bridgeExecutor.postError(
                callbackId,
                Gson().toJson(
                    MiniAppBridgeErrorModel(
                        message = "$ERR_GET_ACCESS_TOKEN ${e.message}"
                    )
                )
            )
        }
    }

    @Suppress("LongMethod")
    private fun onHasAccessTokenPermission(callbackObj: AccessTokenCallbackObj) {
        val tokenPermission = parseAccessTokenPermission(callbackObj)
        doesAccessTokenMatch(tokenPermission) { success, error ->
            if (success) {
                val successCallback = { accessToken: TokenData ->
                    accessToken.scopes = tokenPermission
                    bridgeExecutor.postValue(callbackObj.id, Gson().toJson(accessToken))
                }

                // send a specifically formatted error key to show specific error in mini app
                val errorCallback = { callback: MiniAppAccessTokenError ->
                    // convert error callback to common MiniAppBridgeErrorModel
                    val errorBridgeModel = MiniAppBridgeErrorModel(callback.type, callback.message)
                    bridgeExecutor.postError(callbackObj.id, Gson().toJson(errorBridgeModel))
                }

                userInfoBridgeDispatcher.getAccessToken(
                    miniAppId,
                    tokenPermission,
                    successCallback,
                    errorCallback
                )
            } else {
                bridgeExecutor.postError(
                    callbackObj.id,
                    Gson().toJson(MiniAppBridgeErrorModel(error?.type, error?.message))
                )
            }
        }
    }

    @Suppress("LongMethod", "ComplexMethod")
    private fun doesAccessTokenMatch(
        tokenScope: AccessTokenScope,
        callback: (Boolean, MiniAppAccessTokenError?) -> Unit
    ) {
        if (tokenScope.scopes.isNotEmpty()) {
            val listOfAccessTokenScopesCached = downloadedManifestCache.getAccessTokenPermissions(miniAppId)
            val audience: AccessTokenScope? =
                listOfAccessTokenScopesCached.firstOrNull { it.audience == tokenScope.audience }
                    ?: return callback(false, MiniAppAccessTokenError.audienceNotSupportedError)
            if (audience?.scopes?.containsAll(tokenScope.scopes) != true) {
                callback(false, MiniAppAccessTokenError.scopesNotSupportedError)
            } else {
                return callback(true, null)
            }
        } else {
            callback(
                false,
                MiniAppAccessTokenError.custom(
                    message = "$ERR_GET_ACCESS_TOKEN $ERR_ACCESS_TOKEN_NOT_MATCH_MANIFEST"
                )
            )
        }
    }

    private fun parseAccessTokenPermission(callbackObj: AccessTokenCallbackObj) =
        callbackObj.param ?: AccessTokenScope("", emptyList())

    fun onGetContacts(callbackId: String) = whenReady(callbackId) {
        try {
            if (customPermissionCache.hasPermission(miniAppId, MiniAppCustomPermissionType.CONTACT_LIST)) {
                val successCallback = { contacts: ArrayList<Contact> ->
                    bridgeExecutor.postValue(callbackId, Gson().toJson(contacts))
                }
                val errorCallback = { message: String ->
                    bridgeExecutor.postError(callbackId, "$ERR_GET_CONTACTS $message")
                }

                userInfoBridgeDispatcher.getContacts(successCallback, errorCallback)
            } else {
                bridgeExecutor.postError(callbackId, "$ERR_GET_CONTACTS $ERR_GET_CONTACTS_NO_PERMISSION")
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_GET_CONTACTS ${e.message}")
        }
    }

    @Suppress("LongMethod")
    fun onGetPoints(callbackId: String) = whenReady(callbackId) {
        try {
            if (customPermissionCache.hasPermission(miniAppId, MiniAppCustomPermissionType.POINTS)) {
                val successCallback = { points: Points ->
                    bridgeExecutor.postValue(callbackId, Gson().toJson(points))
                }
                val errorCallback = { callback: MiniAppPointsError ->
                    val errorBridgeModel = MiniAppBridgeErrorModel(callback.type, callback.message)
                    bridgeExecutor.postError(callbackId, Gson().toJson(errorBridgeModel))
                }

                userInfoBridgeDispatcher.getPoints(successCallback, errorCallback)
            } else {
                bridgeExecutor.postError(callbackId,
                        Gson().toJson(MiniAppBridgeErrorModel(
                                "$ERR_GET_POINTS $ERR_GET_POINTS_NO_PERMISSION")
                        )
                )
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId,
                    Gson().toJson(MiniAppBridgeErrorModel(
                            "$ERR_GET_POINTS ${e.message}")
                    )
            )
        }
    }

    @VisibleForTesting
    internal companion object {
        const val ERR_GET_USER_NAME = "Cannot get user name:"
        const val ERR_USER_NAME_NO_PERMISSION =
            "Permission has not been accepted yet for getting user name."
        const val ERR_GET_PROFILE_PHOTO = "Cannot get profile photo:"
        const val ERR_PROFILE_PHOTO_NO_PERMISSION =
            "Permission has not been accepted yet for getting profile photo."
        const val ERR_GET_ACCESS_TOKEN = "Cannot get access token:"
        const val ERR_ACCESS_TOKEN_NO_PERMISSION =
            "Permission has not been accepted yet for getting access token."
        const val ERR_ACCESS_TOKEN_NOT_MATCH_MANIFEST = "Not match the audience and scope in manifest."
        const val ERR_GET_CONTACTS = "Cannot get contacts:"
        const val ERR_GET_CONTACTS_NO_PERMISSION =
            "Permission has not been accepted yet for getting contacts."
        const val ERR_GET_POINTS = "Cannot get points:"
        const val ERR_GET_POINTS_NO_PERMISSION =
                "Permission has not been accepted yet for getting points."
    }
}
