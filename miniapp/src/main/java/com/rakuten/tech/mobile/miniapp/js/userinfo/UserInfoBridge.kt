package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import java.util.ArrayList

@Suppress("TooGenericExceptionCaught")
internal class UserInfoBridge {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var miniAppId: String
    private var isMiniAppComponentReady = false
    private lateinit var userInfoBridgeDispatcher: UserInfoBridgeDispatcher

    fun setMiniAppComponents(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        miniAppId: String
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.customPermissionCache = miniAppCustomPermissionCache
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
                bridgeExecutor.postError(callbackId, "The `UserInfoBridgeDispatcher` $NO_IMPL")
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

    internal fun onGetAccessToken(callbackId: String) = whenReady(callbackId) {
        try {
            val successCallback = { accessToken: TokenData ->
                bridgeExecutor.postValue(callbackId, Gson().toJson(accessToken))
            }
            val errorCallback = { message: String ->
                bridgeExecutor.postError(callbackId, "$ERR_GET_ACCESS_TOKEN $message")
            }

            userInfoBridgeDispatcher.getAccessToken(miniAppId, successCallback, errorCallback)
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_GET_ACCESS_TOKEN ${e.message}")
        }
    }

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
        const val ERR_GET_CONTACTS = "Cannot get contacts:"
        const val ERR_GET_CONTACTS_NO_PERMISSION =
            "Permission has not been accepted yet for getting contacts."
    }
}
