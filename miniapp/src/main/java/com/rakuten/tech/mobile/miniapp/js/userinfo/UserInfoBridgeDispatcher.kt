package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import java.util.ArrayList

/**
 * A class to provide the interfaces for getting user info e.g. user-name, profile-photo etc.
 */
@Suppress("TooGenericExceptionCaught", "LongMethod", "UnnecessaryAbstractClass")
abstract class UserInfoBridgeDispatcher {

    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var miniAppId: String

    /**
     * Get user name from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    open fun getUserName(): String =
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getUserName` $NO_IMPL")

    /**
     * Get profile photo url from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    open fun getProfilePhoto(): String =
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getProfilePhoto` $NO_IMPL")

    /** Get access token from host app. **/
    open fun getAccessToken(
        miniAppId: String,
        onSuccess: (tokenData: TokenData) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getAccessToken` $NO_IMPL")
    }

    /**
     * Get contacts from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    open fun getContacts(
        onSuccess: (contacts: ArrayList<Contact>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getContacts` $NO_IMPL")
    }

    internal fun init(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        miniAppId: String
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.customPermissionCache = miniAppCustomPermissionCache
        this.miniAppId = miniAppId
    }

    internal fun onGetUserName(callbackId: String) {
        try {
            if (customPermissionCache.hasPermission(
                    miniAppId,
                    MiniAppCustomPermissionType.USER_NAME
                )
            ) {
                val name = getUserName()
                if (name.isNotEmpty()) bridgeExecutor.postValue(callbackId, name)
                else bridgeExecutor.postError(
                    callbackId,
                    "$ERR_GET_USER_NAME User name is not found."
                )
            } else
                bridgeExecutor.postError(
                    callbackId,
                    "$ERR_GET_USER_NAME $ERR_USER_NAME_NO_PERMISSION"
                )
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_GET_USER_NAME ${e.message}")
        }
    }

    internal fun onGetProfilePhoto(callbackId: String) {
        try {
            if (customPermissionCache.hasPermission(
                    miniAppId,
                    MiniAppCustomPermissionType.PROFILE_PHOTO
                )
            ) {
                val photoUrl = getProfilePhoto()
                if (photoUrl.isNotEmpty())
                    bridgeExecutor.postValue(callbackId, photoUrl)
                else bridgeExecutor.postError(
                    callbackId,
                    "$ERR_GET_PROFILE_PHOTO Profile photo is not found."
                )
            } else
                bridgeExecutor.postError(
                    callbackId,
                    "$ERR_GET_PROFILE_PHOTO $ERR_PROFILE_PHOTO_NO_PERMISSION"
                )
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_GET_PROFILE_PHOTO ${e.message}")
        }
    }

    internal fun onGetAccessToken(callbackId: String) = try {
        val successCallback = { accessToken: TokenData ->
            bridgeExecutor.postValue(callbackId, Gson().toJson(accessToken))
        }
        val errorCallback = { message: String ->
            bridgeExecutor.postError(callbackId, "$ERR_GET_ACCESS_TOKEN $message")
        }

        getAccessToken(miniAppId, successCallback, errorCallback)
    } catch (e: Exception) {
        bridgeExecutor.postError(callbackId, "$ERR_GET_ACCESS_TOKEN ${e.message}")
    }

    internal fun onGetContacts(callbackId: String) = try {
        if (customPermissionCache.hasPermission(
                miniAppId, MiniAppCustomPermissionType.CONTACT_LIST
            )
        ) {
            val successCallback = { contacts: ArrayList<Contact> ->
                bridgeExecutor.postValue(callbackId, Gson().toJson(contacts))
            }
            val errorCallback = { message: String ->
                bridgeExecutor.postError(callbackId, "$ERR_GET_CONTACTS $message")
            }

            getContacts(successCallback, errorCallback)
        } else {
            bridgeExecutor.postError(
                callbackId,
                "$ERR_GET_CONTACTS $ERR_GET_CONTACTS_NO_PERMISSION"
            )
        }
    } catch (e: Exception) {
        bridgeExecutor.postError(callbackId, "$ERR_GET_CONTACTS ${e.message}")
    }

    @VisibleForTesting
    internal companion object {
        const val NO_IMPL = "method has not been implemented by the Host App."
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
