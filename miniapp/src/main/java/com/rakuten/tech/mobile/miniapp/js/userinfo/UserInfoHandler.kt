package com.rakuten.tech.mobile.miniapp.js.userinfo

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridgeListener
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

/**
 * A class to provide the interfaces for getting user info e.g. user-name, profile-photo etc.
 */
@Suppress("TooGenericExceptionCaught", "LongMethod")
open class UserInfoHandler {

    private lateinit var bridgeListener: MiniAppMessageBridgeListener
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var miniAppId: String

    /** Get user name from host app. **/
    open fun getUserName(): String {
        throw MiniAppSdkException(
            "The `UserInfoHandler.getUserName`" +
                    " method has not been implemented by the Host App."
        )
    }

    /** Get profile photo url from host app. **/
    open fun getProfilePhoto(): String {
        throw MiniAppSdkException(
            "The `UserInfoHandler.getProfilePhoto`" +
                    " method has not been implemented by the Host App."
        )
    }

    internal fun init(
        miniAppMessageBridgeListener: MiniAppMessageBridgeListener,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        miniAppId: String
    ) {
        this.bridgeListener = miniAppMessageBridgeListener
        this.customPermissionCache = miniAppCustomPermissionCache
        this.miniAppId = miniAppId
    }

    internal fun onGetUserName(callbackId: String) {
        try {
            var isPermissionGranted = false
            customPermissionCache.readPermissions(miniAppId).pairValues.find {
                it.first == MiniAppCustomPermissionType.USER_NAME &&
                        it.second == MiniAppCustomPermissionResult.ALLOWED
            }?.let { isPermissionGranted = true }

            if (isPermissionGranted) {
                val name = getUserName()
                if (name.isNotEmpty()) bridgeListener.postValue(callbackId, name)
                else bridgeListener.postError(
                    callbackId,
                    "$ERR_GET_USER_NAME User name is not found."
                )
            } else
                bridgeListener.postError(
                    callbackId,
                    "$ERR_GET_USER_NAME $ERR_USER_NAME_NO_PERMISSION"
                )
        } catch (e: Exception) {
            bridgeListener.postError(callbackId, "$ERR_GET_USER_NAME ${e.message}")
        }
    }

    internal fun onGetProfilePhoto(callbackId: String) {
        try {
            var isPermissionGranted = false
            customPermissionCache.readPermissions(miniAppId).pairValues.find {
                it.first == MiniAppCustomPermissionType.PROFILE_PHOTO &&
                        it.second == MiniAppCustomPermissionResult.ALLOWED
            }?.let { isPermissionGranted = true }

            if (isPermissionGranted) {
                val photoUrl = getProfilePhoto()
                if (photoUrl.isNotEmpty())
                    bridgeListener.postValue(callbackId, photoUrl)
                else bridgeListener.postError(
                    callbackId,
                    "$ERR_GET_PROFILE_PHOTO Profile photo is not found."
                )
            } else
                bridgeListener.postError(
                    callbackId,
                    "$ERR_GET_PROFILE_PHOTO $ERR_PROFILE_PHOTO_NO_PERMISSION"
                )
        } catch (e: Exception) {
            bridgeListener.postError(callbackId, "$ERR_GET_PROFILE_PHOTO ${e.message}")
        }
    }

    private companion object {
        const val ERR_GET_USER_NAME = "Cannot get user name:"
        const val ERR_USER_NAME_NO_PERMISSION =
            "Permission has not been accepted yet for getting user name."
        const val ERR_GET_PROFILE_PHOTO = "Cannot get profile photo:"
        const val ERR_PROFILE_PHOTO_NO_PERMISSION =
            "Permission has not been accepted yet for getting profile photo."
    }
}
