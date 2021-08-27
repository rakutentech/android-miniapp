package com.rakuten.tech.mobile.miniapp.js

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

    @Suppress("ComplexMethod")
    private fun getAcType(action: String): Actype {
        return when (action) {
            ActionType.GET_UNIQUE_ID.action -> Actype.GET_UNIQUE_ID
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
            else -> Actype.DEFAULT
        }
    }
}
