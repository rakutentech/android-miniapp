package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

/**
 * A class to provide the interfaces for getting user info e.g. user-name, profile-photo etc.
 */
@Suppress("TooGenericExceptionCaught", "LongMethod", "UnnecessaryAbstractClass")
abstract class UserInfoBridgeDispatcher {

    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var miniAppId: String

    /** Get user name from host app. **/
    open fun getUserName(): String =
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getUserName` $NO_IMPL")

    /** Get profile photo url from host app. **/
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
            var isPermissionGranted = false
            customPermissionCache.readPermissions(miniAppId).pairValues.find {
                it.first == MiniAppCustomPermissionType.USER_NAME &&
                        it.second == MiniAppCustomPermissionResult.ALLOWED
            }?.let { isPermissionGranted = true }

            if (isPermissionGranted) {
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
            var isPermissionGranted = false
            customPermissionCache.readPermissions(miniAppId).pairValues.find {
                it.first == MiniAppCustomPermissionType.PROFILE_PHOTO &&
                        it.second == MiniAppCustomPermissionResult.ALLOWED
            }?.let { isPermissionGranted = true }

            if (isPermissionGranted) {
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
    }
}
