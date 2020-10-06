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
                it.first == MiniAppCustomPermissionType.USER_NAME && it.second == MiniAppCustomPermissionResult.ALLOWED
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

    private companion object {
        const val ERR_GET_USER_NAME = "Cannot get user name:"
        const val ERR_USER_NAME_NO_PERMISSION =
            "Permission has not been accepted yet for getting user name."
    }
}
