package com.rakuten.tech.mobile.miniapp.js

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.analytics.Actype
import com.rakuten.tech.mobile.miniapp.analytics.Etype
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics

internal class MessageBridgeRatDispatcher(private val miniAppAnalytics: MiniAppAnalytics) {
    fun sendAnalyticsSdkFeature(action: String) {
        miniAppAnalytics.sendAnalytics(
            eType = Etype.CLICK,
            actype = getAcType(action),
            miniAppInfo = null
        )
    }

    @Suppress("ComplexMethod", "LongMethod")
    @VisibleForTesting
    internal fun getAcType(action: String): Actype {
        return when (action) {
            ActionType.GET_UNIQUE_ID.action -> Actype.GET_UNIQUE_ID
            ActionType.GET_MESSAGING_UNIQUE_ID.action -> Actype.GET_MESSAGING_UNIQUE_ID
            ActionType.GET_MAUID.action -> Actype.GET_MAUID
            ActionType.REQUEST_PERMISSION.action -> Actype.REQUEST_PERMISSION
            ActionType.REQUEST_CUSTOM_PERMISSIONS.action -> Actype.REQUEST_CUSTOM_PERMISSIONS
            ActionType.SHARE_INFO.action -> Actype.SHARE_INFO
            ActionType.LOAD_AD.action -> Actype.LOAD_AD
            ActionType.SHOW_AD.action -> Actype.SHOW_AD
            ActionType.GET_USER_NAME.action -> Actype.GET_USER_NAME
            ActionType.GET_PROFILE_PHOTO.action -> Actype.GET_PROFILE_PHOTO
            ActionType.GET_ACCESS_TOKEN.action -> Actype.GET_ACCESS_TOKEN
            ActionType.GET_POINTS.action -> Actype.GET_POINTS
            ActionType.SET_SCREEN_ORIENTATION.action -> Actype.SET_SCREEN_ORIENTATION
            ActionType.GET_CONTACTS.action -> Actype.GET_CONTACTS
            ActionType.SEND_MESSAGE_TO_CONTACT.action -> Actype.SEND_MESSAGE_TO_CONTACT
            ActionType.SEND_MESSAGE_TO_CONTACT_ID.action -> Actype.SEND_MESSAGE_TO_CONTACT_ID
            ActionType.SEND_MESSAGE_TO_MULTIPLE_CONTACTS.action -> Actype.SEND_MESSAGE_TO_MULTIPLE_CONTACTS
            ActionType.GET_HOST_ENVIRONMENT_INFO.action -> Actype.GET_HOST_ENVIRONMENT_INFO
            ActionType.FILE_DOWNLOAD.action -> Actype.FILE_DOWNLOAD
            ActionType.SECURE_STORAGE_SET_ITEMS.action -> Actype.SECURE_STORAGE_SET_ITEMS
            ActionType.SECURE_STORAGE_GET_ITEM.action -> Actype.SECURE_STORAGE_GET_ITEM
            ActionType.SECURE_STORAGE_REMOVE_ITEMS.action -> Actype.SECURE_STORAGE_REMOVE_ITEMS
            ActionType.SECURE_STORAGE_CLEAR.action -> Actype.SECURE_STORAGE_CLEAR
            ActionType.SECURE_STORAGE_SIZE.action -> Actype.SECURE_STORAGE_SIZE
            ActionType.SET_CLOSE_ALERT.action -> Actype.SET_CLOSE_ALERT
            ActionType.GET_PURCHASE_ITEM_LIST.action -> Actype.GET_PURCHASE_ITEM_LIST
            ActionType.PURCHASE_ITEM.action -> Actype.PURCHASE_ITEM
            ActionType.CONSUME_PURCHASE.action -> Actype.CONSUME_PURCHASE
            ActionType.JSON_INFO.action -> Actype.JSON_INFO
            ActionType.CLOSE_MINIAPP.action -> Actype.CLOSE_MINIAPP
            ActionType.GET_HOST_APP_THEME_COLORS.action -> Actype.GET_HOST_APP_THEME_COLORS
            ActionType.GET_IS_DARK_MODE.action -> Actype.GET_IS_DARK_MODE
            ActionType.SEND_MA_ANALYTICS.action -> Actype.SEND_MA_ANALYTICS
            else -> Actype.DEFAULT
        }
    }
}
