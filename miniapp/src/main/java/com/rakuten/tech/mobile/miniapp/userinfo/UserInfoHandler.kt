package com.rakuten.tech.mobile.miniapp.userinfo

import com.rakuten.tech.mobile.miniapp.js.CallbackObj
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

@Suppress("TooGenericExceptionCaught", "LongMethod")
internal class UserInfoHandler(private val bridge: MiniAppMessageBridge) {

    fun onGetUserName(callbackObj: CallbackObj) {
        try {
            var isPermissionGranted = false
            bridge.customPermissionCache.readPermissions(bridge.miniAppInfo.id).pairValues.find {
                it.first == MiniAppCustomPermissionType.USER_NAME &&
                        it.second == MiniAppCustomPermissionResult.ALLOWED
            }?.let { isPermissionGranted = true }

            if (isPermissionGranted) {
                val name = bridge.getUserName()
                if (name.isNotEmpty()) bridge.postValue(callbackObj.id, name)
                else bridge.postError(
                    callbackObj.id,
                    "$ERR_GET_USER_NAME User name is not found."
                )
            } else
                bridge.postError(
                    callbackObj.id,
                    "$ERR_GET_USER_NAME $ERR_USER_NAME_NO_PERMISSION"
                )
        } catch (e: Exception) {
            bridge.postError(
                callbackObj.id,
                "$ERR_GET_USER_NAME ${e.message}"
            )
        }
    }

    fun onGetProfilePhoto(callbackObj: CallbackObj) {
        try {
            var isPermissionGranted = false
            bridge.customPermissionCache.readPermissions(bridge.miniAppInfo.id).pairValues.find {
                it.first == MiniAppCustomPermissionType.PROFILE_PHOTO &&
                        it.second == MiniAppCustomPermissionResult.ALLOWED
            }?.let { isPermissionGranted = true }

            if (isPermissionGranted) {
                val photoUrl = bridge.getProfilePhoto()
                if (photoUrl.isNotEmpty())
                    bridge.postValue(callbackObj.id, photoUrl)
                else bridge.postError(
                    callbackObj.id,
                    "$ERR_GET_PROFILE_PHOTO Profile photo is not found."
                )
            } else
                bridge.postError(
                    callbackObj.id,
                    "$ERR_GET_PROFILE_PHOTO $ERR_PROFILE_PHOTO_NO_PERMISSION"
                )
        } catch (e: Exception) {
            bridge.postError(
                callbackObj.id,
                "$ERR_GET_PROFILE_PHOTO ${e.message}"
            )
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
