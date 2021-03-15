package com.rakuten.tech.mobile.miniapp.js.userinfo

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL
import java.util.ArrayList

/**
 * A class to provide the interfaces for getting user info e.g. user-name, profile-photo etc.
 */
interface UserInfoBridgeDispatcher {

    /**
     * Get user name from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    fun getUserName(
        onSuccess: (userName: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getUserName` $NO_IMPL")
    }

    /**
     * Get profile photo url from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    fun getProfilePhoto(
        onSuccess: (profilePhoto: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getProfilePhoto` $NO_IMPL")
    }

    /** Get access token from host app. **/
    fun getAccessToken(
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
    fun getContacts(
        onSuccess: (contacts: ArrayList<Contact>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getContacts` $NO_IMPL")
    }
}
