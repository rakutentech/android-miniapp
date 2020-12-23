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
    @Deprecated(
        "This function has been deprecated.",
        ReplaceWith("getUserName(onSuccess: (name: String) -> Unit, onError: (message: String) -> Unit)")
    )
    open fun getUserName(): String =
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getUserName` $NO_IMPL")

    /**
     * Get user name from host app.
     * You can send user name or throw an [Exception] from this method by passing a [Result].
     */
    open fun getUserName(
        callback: (userName: Result<String?>) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getUserName` $NO_IMPL")
    }

    /**
     * Get profile photo url from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    @Deprecated(
        "This function has been deprecated.",
        ReplaceWith("getProfilePhoto(onSuccess: (photoUrl: String) -> Unit, onError: (message: String) -> Unit)")
    )
    open fun getProfilePhoto(): String =
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getProfilePhoto` $NO_IMPL")

    /**
     * Get profile photo url from host app.
     * You can send profile photo url or throw an [Exception] from this method by passing a [Result].
     */
    open fun getProfilePhoto(
        callback: (profilePhoto: Result<String?>) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getProfilePhoto` $NO_IMPL")
    }

    /** Get access token from host app. **/
    @Deprecated(
        "This function has been deprecated.",
        ReplaceWith("getAccessToken(miniAppId: String, callback: (result: Result<TokenData?>) -> Unit)")
    )
    open fun getAccessToken(
        miniAppId: String,
        onSuccess: (tokenData: TokenData) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getAccessToken` $NO_IMPL")
    }

    /** Get access token from host app.
     * You can send access token or throw an [Exception] from this method by passing a [Result].
     * */
    open fun getAccessToken(
        miniAppId: String,
        callback: (accessToken: Result<TokenData?>) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getAccessToken` $NO_IMPL")
    }

    /**
     * Get contacts from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    @Deprecated(
        "This function has been deprecated.",
        ReplaceWith("getContacts(callback: (result: Result<ArrayList<Contact>?>) -> Unit)")
    )
    open fun getContacts(
        onSuccess: (contacts: ArrayList<Contact>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getContacts` $NO_IMPL")
    }

    /**
     * Get contacts from host app.
     * You can send contacts or throw an [Exception] from this method by passing a [Result].
     */
    open fun getContacts(
        callback: (contacts: Result<ArrayList<Contact>?>) -> Unit
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

    /** region: user name */
    internal fun onGetUserName(callbackId: String) {
        if (customPermissionCache.hasPermission(
                miniAppId,
                MiniAppCustomPermissionType.USER_NAME
            )
        ) {
            try {
                // asynchronously retrieve user name
                val resultCallback = { result: Result<String?> ->
                    if (result.isSuccess) bridgeExecutor.postValue(
                        callbackId,
                        result.getOrNull().toString()
                    )
                    else if (result.isFailure) bridgeExecutor.postError(
                        callbackId,
                        "$ERR_GET_USER_NAME ${result.exceptionOrNull()?.message}"
                    )
                }
                getUserName(resultCallback)
            } catch (e: Exception) {
                if (e.message.toString().contains(NO_IMPL)) getUserNameSync(callbackId)
                else bridgeExecutor.postError(callbackId, "$ERR_GET_USER_NAME ${e.message}")
            }
        } else
            bridgeExecutor.postError(
                callbackId,
                "$ERR_GET_USER_NAME $ERR_USER_NAME_NO_PERMISSION"
            )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getUserNameSync(callbackId: String) {
        try {
            val name = getUserName()
            if (name.isNotEmpty()) bridgeExecutor.postValue(callbackId, name)
            else bridgeExecutor.postError(
                callbackId,
                "$ERR_GET_USER_NAME User name is not found."
            )
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_GET_USER_NAME ${e.message}")
        }
    }
    /** end region */

    /** region: profile photo */
    internal fun onGetProfilePhoto(callbackId: String) {
        if (customPermissionCache.hasPermission(
                miniAppId,
                MiniAppCustomPermissionType.PROFILE_PHOTO
            )
        ) {
            try {
                // asynchronously retrieve profile photo url
                val resultCallback = { result: Result<String?> ->
                    if (result.isSuccess) bridgeExecutor.postValue(
                        callbackId,
                        result.getOrNull().toString()
                    )
                    else if (result.isFailure) bridgeExecutor.postError(
                        callbackId,
                        "$ERR_GET_PROFILE_PHOTO ${result.exceptionOrNull()?.message}"
                    )
                }
                getProfilePhoto(resultCallback)
            } catch (e: Exception) {
                if (e.message.toString().contains(NO_IMPL)) getProfilePhotoSync(callbackId)
                else bridgeExecutor.postError(callbackId, "$ERR_GET_PROFILE_PHOTO ${e.message}")
            }
        } else
            bridgeExecutor.postError(
                callbackId,
                "$ERR_GET_PROFILE_PHOTO $ERR_PROFILE_PHOTO_NO_PERMISSION"
            )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getProfilePhotoSync(callbackId: String) {
        try {
            val photoUrl = getProfilePhoto()
            if (photoUrl.isNotEmpty()) bridgeExecutor.postValue(callbackId, photoUrl)
            else bridgeExecutor.postError(
                callbackId,
                "$ERR_GET_PROFILE_PHOTO Profile photo is not found."
            )
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_GET_PROFILE_PHOTO ${e.message}")
        }
    }
    /** end region */

    /** region: access token */
    internal fun onGetAccessToken(callbackId: String) {
        try {
            // retrieve access token using Result
            val resultCallback = { result: Result<TokenData?> ->
                if (result.isSuccess) bridgeExecutor.postValue(
                    callbackId,
                    Gson().toJson(result.getOrNull())
                )
                else if (result.isFailure) bridgeExecutor.postError(
                    callbackId,
                    "$ERR_GET_ACCESS_TOKEN ${result.exceptionOrNull()?.message}"
                )
            }
            getAccessToken(miniAppId, resultCallback)
        } catch (e: Exception) {
            if (e.message.toString().contains(NO_IMPL)) getAccessTokenWithoutResult(callbackId)
            else bridgeExecutor.postError(callbackId, "$ERR_GET_ACCESS_TOKEN ${e.message}")
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getAccessTokenWithoutResult(callbackId: String) {
        try {
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
    }
    /** end region */

    /** region: contacts */
    internal fun onGetContacts(callbackId: String) = try {
        if (customPermissionCache.hasPermission(
                miniAppId, MiniAppCustomPermissionType.CONTACT_LIST
            )
        ) {
            try {
                // retrieve contacts using Result
                val resultCallback = { result: Result<ArrayList<Contact>?> ->
                    if (result.isSuccess) bridgeExecutor.postValue(
                        callbackId,
                        Gson().toJson(result.getOrNull())
                    )
                    else if (result.isFailure) bridgeExecutor.postError(
                        callbackId,
                        "$ERR_GET_CONTACTS ${result.exceptionOrNull()?.message}"
                    )
                }
                getContacts(resultCallback)
            } catch (e: Exception) {
                if (e.message.toString().contains(NO_IMPL)) getContactsWithoutResult(callbackId)
                else bridgeExecutor.postError(callbackId, "$ERR_GET_CONTACTS ${e.message}")
            }
        } else {
            bridgeExecutor.postError(
                callbackId,
                "$ERR_GET_CONTACTS $ERR_GET_CONTACTS_NO_PERMISSION"
            )
        }
    } catch (e: Exception) {
        bridgeExecutor.postError(callbackId, "$ERR_GET_CONTACTS ${e.message}")
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getContactsWithoutResult(callbackId: String) {
        try {
            val successCallback = { contacts: ArrayList<Contact> ->
                bridgeExecutor.postValue(callbackId, Gson().toJson(contacts))
            }
            val errorCallback = { message: String ->
                bridgeExecutor.postError(callbackId, "$ERR_GET_CONTACTS $message")
            }
            getContacts(successCallback, errorCallback)
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_GET_CONTACTS ${e.message}")
        }
    }
    /** end region */

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
