package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.js.CallbackObj
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import java.util.ArrayList

@Suppress("TooGenericExceptionCaught")
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
                val successCallback = { userName: String -> bridgeExecutor.postValue(callbackId, userName) }
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

    internal fun onGetAccessToken(callbackObj: CallbackObj) = whenReady(callbackObj.id) {
        try {
            if (customPermissionCache.hasPermission(miniAppId, MiniAppCustomPermissionType.ACCESS_TOKEN)) {
                onHasAccessTokenPermission(callbackObj)
            } else
                bridgeExecutor.postError(callbackObj.id, "$ERR_GET_ACCESS_TOKEN $ERR_ACCESS_TOKEN_NO_PERMISSION")
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackObj.id, "$ERR_GET_ACCESS_TOKEN ${e.message}")
        }
    }

    private fun onHasAccessTokenPermission(callbackObj: CallbackObj) {
        val tokenPermission = parseAccessTokenPermission(callbackObj)
        if (doesAccessTokenMatch(tokenPermission)) {
            val successCallback = { accessToken: TokenData ->
                accessToken.scopes = tokenPermission
                bridgeExecutor.postValue(callbackObj.id, Gson().toJson(accessToken))
            }
            val errorCallback = { message: String ->
                bridgeExecutor.postError(callbackObj.id, "$ERR_GET_ACCESS_TOKEN $message")
            }

            userInfoBridgeDispatcher.getAccessToken(miniAppId, tokenPermission, successCallback, errorCallback)
        } else
            bridgeExecutor.postError(callbackObj.id, "$ERR_GET_ACCESS_TOKEN $ERR_ACCESS_TOKEN_NOT_MATCH_MANIFEST")
    }

    private fun doesAccessTokenMatch(tokenScope: AccessTokenScope): Boolean {
        if (tokenScope.scopes.isNotEmpty()) {
            for (atc in downloadedManifestCache.getAccessTokenPermissions(miniAppId)) {
                if (atc.audience == tokenScope.audience && atc.scopes.containsAll(tokenScope.scopes))
                    return true
            }
        }
        return false
    }

    private fun parseAccessTokenPermission(callbackObj: CallbackObj) = Gson().fromJson<AccessTokenScope>(
            callbackObj.param.toString(),
            object : TypeToken<AccessTokenScope>() {}.type
    ) ?: AccessTokenScope("", emptyList())

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
    }
}
