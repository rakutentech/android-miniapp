package com.rakuten.tech.mobile.miniapp.js

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class ActionTypeSpec {

    @Test
    fun `ActionType values should match with the required values`() {
        ActionType.GET_UNIQUE_ID.action shouldBeEqualTo "getUniqueId"
        ActionType.GET_MESSAGING_UNIQUE_ID.action shouldBeEqualTo "getMessagingUniqueId"
        ActionType.GET_MAUID.action shouldBeEqualTo "getMauid"
        ActionType.REQUEST_PERMISSION.action shouldBeEqualTo "requestPermission"
        ActionType.REQUEST_CUSTOM_PERMISSIONS.action shouldBeEqualTo "requestCustomPermissions"
        ActionType.SHARE_INFO.action shouldBeEqualTo "shareInfo"
        ActionType.LOAD_AD.action shouldBeEqualTo "loadAd"
        ActionType.SHOW_AD.action shouldBeEqualTo "showAd"
        ActionType.GET_USER_NAME.action shouldBeEqualTo "getUserName"
        ActionType.GET_PROFILE_PHOTO.action shouldBeEqualTo "getProfilePhoto"
        ActionType.GET_ACCESS_TOKEN.action shouldBeEqualTo "getAccessToken"
        ActionType.SET_SCREEN_ORIENTATION.action shouldBeEqualTo "setScreenOrientation"
        ActionType.GET_CONTACTS.action shouldBeEqualTo "getContacts"
        ActionType.SEND_MESSAGE_TO_CONTACT.action shouldBeEqualTo "sendMessageToContact"
        ActionType.SEND_MESSAGE_TO_CONTACT_ID.action shouldBeEqualTo "sendMessageToContactId"
        ActionType.SEND_MESSAGE_TO_MULTIPLE_CONTACTS.action shouldBeEqualTo "sendMessageToMultipleContacts"
        ActionType.GET_POINTS.action shouldBeEqualTo "getPoints"
        ActionType.GET_HOST_ENVIRONMENT_INFO.action shouldBeEqualTo "getHostEnvironmentInfo"
        ActionType.FILE_DOWNLOAD.action shouldBeEqualTo "downloadFile"
        ActionType.SECURE_STORAGE_SET_ITEMS.action shouldBeEqualTo "setSecureStorageItems"
        ActionType.SECURE_STORAGE_GET_ITEM.action shouldBeEqualTo "getSecureStorageItem"
        ActionType.SECURE_STORAGE_REMOVE_ITEMS.action shouldBeEqualTo "removeSecureStorageItems"
        ActionType.SECURE_STORAGE_CLEAR.action shouldBeEqualTo "clearSecureStorage"
        ActionType.SECURE_STORAGE_SIZE.action shouldBeEqualTo "getSecureStorageSize"
        ActionType.SET_CLOSE_ALERT.action shouldBeEqualTo "setCloseAlert"
        ActionType.JSON_INFO.action shouldBeEqualTo "sendJsonToHostapp"
        ActionType.CLOSE_MINIAPP.action shouldBeEqualTo "miniAppShouldClose"
    }

    @Test
    fun `ScreenOrientation values should match with the required values`() {
        ScreenOrientation.LOCK_PORTRAIT.value shouldBeEqualTo "rakuten.miniapp.screen.LOCK_PORTRAIT"
        ScreenOrientation.LOCK_LANDSCAPE.value shouldBeEqualTo "rakuten.miniapp.screen.LOCK_LANDSCAPE"
        ScreenOrientation.LOCK_RELEASE.value shouldBeEqualTo "rakuten.miniapp.screen.LOCK_RELEASE"
    }

    @Test
    fun `NativeEventType values should match with the required values`() {
        NativeEventType.EXTERNAL_WEBVIEW_CLOSE.value shouldBeEqualTo "miniappwebviewclosed"
        NativeEventType.MINIAPP_ON_PAUSE.value shouldBeEqualTo "miniapppause"
        NativeEventType.MINIAPP_ON_RESUME.value shouldBeEqualTo "miniappresume"
        NativeEventType.MINIAPP_SECURE_STORAGE_READY.value shouldBeEqualTo "miniappsecurestorageready"
        NativeEventType.MINIAPP_SECURE_STORAGE_LOAD_ERROR.value shouldBeEqualTo "miniappsecurestorageloaderror"
        NativeEventType.MINIAPP_RECEIVE_JSON_INFO.value shouldBeEqualTo "miniappreceivejsoninfo"
    }

    @Test
    fun `AdType values should match with the required values`() {
        AdType.INTERSTITIAL.value shouldBeEqualTo 0
        AdType.REWARDED.value shouldBeEqualTo 1
    }
}
