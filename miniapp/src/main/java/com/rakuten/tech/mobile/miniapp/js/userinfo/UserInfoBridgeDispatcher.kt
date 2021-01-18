package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import java.util.ArrayList

/**
 * A class to provide the interfaces for getting user info e.g. user-name, profile-photo etc.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class UserInfoBridgeDispatcher {
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
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    open fun getUserName(
        onSuccess: (userName: String) -> Unit,
        onError: (message: String) -> Unit
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
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    open fun getProfilePhoto(
        onSuccess: (profilePhoto: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        throw MiniAppSdkException("The `UserInfoBridgeDispatcher.getProfilePhoto` $NO_IMPL")
    }

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

    @VisibleForTesting
    internal companion object {
        const val NO_IMPL = "method has not been implemented by the Host App."
    }
}
