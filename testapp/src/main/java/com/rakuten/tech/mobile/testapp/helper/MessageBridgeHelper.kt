package com.rakuten.tech.mobile.testapp.helper

import android.app.Activity
import android.content.res.Configuration
import androidx.core.app.ActivityCompat
import com.rakuten.tech.mobile.miniapp.analytics.MAAnalyticsInfo
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.errors.MiniAppPointsError
import com.rakuten.tech.mobile.miniapp.js.MessageToContact
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.js.hostenvironment.HostAppThemeColors
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Points
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.chat.ChatWindow
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings


fun getMessageBridge(
    activity: Activity,
    onDevicePermissionResultCallback: ((Boolean) -> Unit) -> Unit,
) = object : MiniAppMessageBridge() {

    override fun getUniqueId(
        onSuccess: (uniqueId: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val errorMsg = AppSettings.instance.uniqueIdError
        if (errorMsg.isNotEmpty()) onError(errorMsg)
        else onSuccess(AppSettings.instance.uniqueId)
    }

    override fun getMessagingUniqueId(
        onSuccess: (uniqueId: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val errorMsg = AppSettings.instance.messagingUniqueIdError
        if (errorMsg.isNotEmpty()) onError(errorMsg)
        else onSuccess("TEST-MESSAGE_UNIQUE-ID-01234")
    }

    override fun getMauid(
        onSuccess: (mauid: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val errorMsg = AppSettings.instance.mauIdError
        if (errorMsg.isNotEmpty()) onError(errorMsg)
        else onSuccess("TEST-MAUID-01234-56789")
    }

    override fun requestDevicePermission(
        miniAppPermissionType: MiniAppDevicePermissionType,
        callback: (isGranted: Boolean) -> Unit
    ) {
        onDevicePermissionResultCallback.invoke(callback)
        ActivityCompat.requestPermissions(
            activity,
            AppDevicePermission.getDevicePermissionRequest(miniAppPermissionType),
            AppDevicePermission.getDeviceRequestCode(miniAppPermissionType)
        )
    }

    override fun sendJsonToHostApp(
        jsonStr: String,
        onSuccess: (String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        jsonStr.let {
            val message: String
            if (it.isNotBlank()) {
                message = it
                onSuccess(message)
            } else {
                message =
                    activity.getString(R.string.error_send_json_to_host_app_please_try_again)
                onError(message)
            }
            if (activity.isAvailable) {
                activity.showToastMessage(message)
            }
        }
    }

    override fun getHostAppThemeColors(
        onSuccess: (themeColor: HostAppThemeColors) -> Unit,
        onError: (message: String) -> Unit
    ) {
        onSuccess(AppSettings.instance.colorTheme)
    }

    override fun getIsDarkMode(
        onSuccess: (isDarkMode: Boolean) -> Unit,
        onError: (message: String) -> Unit
    ) {
        when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> onSuccess(false) // Night mode is not active, we're using the light theme.
            Configuration.UI_MODE_NIGHT_YES -> onSuccess(true) // Night mode is active, we're using dark theme.
        }
    }

    override fun didReceiveMAAnalytics(
        analyticsInfo: MAAnalyticsInfo,
        onSuccess: (message: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        super.didReceiveMAAnalytics(analyticsInfo, onSuccess, onError)
    }
}

fun getUserInfoBridgeDispatcher() = object : UserInfoBridgeDispatcher {

    override fun getUserName(
        onSuccess: (userName: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val name = AppSettings.instance.profileName
        if (name.isNotEmpty()) onSuccess(name)
        else onError("User name is not found.")
    }

    override fun getProfilePhoto(
        onSuccess: (profilePhoto: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val photo = AppSettings.instance.profilePictureUrlBase64
        if (photo.isNotEmpty()) onSuccess(photo)
        else onError("Profile photo is not found.")
    }

    override fun getAccessToken(
        miniAppId: String,
        accessTokenScope: AccessTokenScope,
        onSuccess: (tokenData: TokenData) -> Unit,
        onError: (tokenError: MiniAppAccessTokenError) -> Unit
    ) {
        if (AppSettings.instance.accessTokenError != null) {
            onError(AppSettings.instance.accessTokenError!!)
        } else {
            onSuccess(AppSettings.instance.tokenData)
        }
    }

    override fun getContacts(
        onSuccess: (contacts: ArrayList<Contact>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (AppSettings.instance.isContactsSaved)
            onSuccess(AppSettings.instance.contacts)
        else
            onError("There is no contact found in HostApp.")
    }

    override fun getPoints(
        onSuccess: (points: Points) -> Unit,
        onError: (pointsError: MiniAppPointsError) -> Unit
    ) {
        val points = AppSettings.instance.points
        if (points != null) onSuccess(points)
        else onError(MiniAppPointsError.custom("There is no points found in HostApp."))
    }
}


fun getChatBridgeDispatcher(chatWindow: ChatWindow) =
    object : ChatBridgeDispatcher {

        override fun sendMessageToContact(
            message: MessageToContact,
            onSuccess: (contactId: String?) -> Unit,
            onError: (message: String) -> Unit
        ) {
            chatWindow.openSingleContactSelection(message, onSuccess, onError)
        }

        override fun sendMessageToContactId(
            contactId: String,
            message: MessageToContact,
            onSuccess: (contactId: String?) -> Unit,
            onError: (message: String) -> Unit
        ) {
            chatWindow.openSpecificContactIdSelection(contactId, message, onSuccess, onError)
        }

        override fun sendMessageToMultipleContacts(
            message: MessageToContact,
            onSuccess: (contactIds: List<String>?) -> Unit,
            onError: (message: String) -> Unit
        ) {
            chatWindow.openMultipleContactSelections(message, onSuccess, onError)
        }
    }


