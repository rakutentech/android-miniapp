package com.rakuten.tech.mobile.miniapp.js.userinfo

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.errors.MiniAppError
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
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
        throw MiniAppSdkException(NO_IMPL)
    }

    /**
     * Get profile photo url from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    fun getProfilePhoto(
        onSuccess: (profilePhoto: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException(NO_IMPL)
    }

    /** Get access token from host app. **/
    @Deprecated("This function has been deprecated.")
    fun getAccessToken(
        miniAppId: String,
        onSuccess: (tokenData: TokenData) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException(NO_IMPL)
    }

    /**
     * Get access token from host app.
     * @param accessTokenScope contains audience and scope for permission validation.
     **/
    @JvmName("getAccessTokenDeprecated")
    @Suppress("INAPPLICABLE_JVM_NAME")
    @Deprecated(
        "This function has been deprecated.",
        ReplaceWith("getAccessToken(miniAppId, accessTokenScope,  onSuccess, onError)")
    )
    fun getAccessToken(
        miniAppId: String,
        accessTokenScope: AccessTokenScope,
        onSuccess: (tokenData: TokenData) -> Unit,
        onError: (message: String) -> Unit
    ) = getAccessToken(miniAppId, onSuccess, onError)

    /**
     * Get access token from host app.
     * @param accessTokenScope contains audience and scope for permission validation.
     * @param onError contains custom error message send from host app
     **/
    @JvmName("getAccessToken")
    @Suppress("INAPPLICABLE_JVM_NAME")
    fun getAccessToken(
        miniAppId: String,
        accessTokenScope: AccessTokenScope,
        onSuccess: (tokenData: TokenData) -> Unit,
        onError: (errorKey: MiniAppError) -> Unit
    ) {
        throw MiniAppSdkException(NO_IMPL)
    }

    /**
     * Get contacts from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    fun getContacts(
        onSuccess: (contacts: ArrayList<Contact>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException(NO_IMPL)
    }
}
