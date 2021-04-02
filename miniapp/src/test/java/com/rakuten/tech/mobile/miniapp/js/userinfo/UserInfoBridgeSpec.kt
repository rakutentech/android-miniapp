package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.mockito.kotlin.*
import org.mockito.kotlin.mock
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_DISPLAY_NAME
import com.rakuten.tech.mobile.miniapp.TEST_MA_ICON
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_TAG
import com.rakuten.tech.mobile.miniapp.TEST_USER_NAME
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.*
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridge.Companion.ERR_ACCESS_TOKEN_NOT_MATCH_MANIFEST
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridge.Companion.ERR_ACCESS_TOKEN_NO_PERMISSION
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridge.Companion.ERR_GET_ACCESS_TOKEN
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridge.Companion.ERR_GET_CONTACTS
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridge.Companion.ERR_GET_CONTACTS_NO_PERMISSION
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridge.Companion.ERR_GET_PROFILE_PHOTO
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridge.Companion.ERR_GET_USER_NAME
import com.rakuten.tech.mobile.miniapp.permission.*
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.util.ArrayList

@Suppress("LongMethod", "LargeClass")
@RunWith(AndroidJUnit4::class)
class UserInfoBridgeSpec {
    private lateinit var miniAppBridge: MiniAppMessageBridge
    private val userNameCallbackObj = CallbackObj(
        action = ActionType.GET_USER_NAME.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    private val profilePhotoCallbackObj = CallbackObj(
        action = ActionType.GET_PROFILE_PHOTO.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    private val contactsCallbackObj = CallbackObj(
        action = ActionType.GET_CONTACTS.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    private val customPermissionCache: MiniAppCustomPermissionCache = mock()
    private val downloadedManifestCache: DownloadedManifestCache = mock()
    private val miniAppInfo = MiniAppInfo(
        id = TEST_MA_ID,
        displayName = TEST_MA_DISPLAY_NAME,
        icon = TEST_MA_ICON,
        version = Version(TEST_MA_VERSION_TAG, TEST_MA_VERSION_ID)
    )
    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))

    @Before
    fun setup() {
        miniAppBridge = Mockito.spy(createMessageBridge())
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = customPermissionCache,
            downloadedManifestCache = downloadedManifestCache,
            miniAppId = TEST_MA.id
        )

        whenever(customPermissionCache.hasPermission(
            miniAppInfo.id, MiniAppCustomPermissionType.USER_NAME)
        ).thenReturn(true)
        whenever(customPermissionCache.hasPermission(
            miniAppInfo.id, MiniAppCustomPermissionType.PROFILE_PHOTO)
        ).thenReturn(true)
        whenever(customPermissionCache.hasPermission(
            miniAppInfo.id, MiniAppCustomPermissionType.CONTACT_LIST)
        ).thenReturn(true)
        whenever(customPermissionCache.hasPermission(
            miniAppInfo.id, MiniAppCustomPermissionType.ACCESS_TOKEN)
        ).thenReturn(true)
        When calling downloadedManifestCache.getAccessTokenPermissions(TEST_MA_ID) itReturns TEST_ATP_LIST
    }

    /** start region: onGetUserName */
    private fun createUserNameImpl(
        hasUserName: Boolean,
        canGetName: Boolean
    ): UserInfoBridgeDispatcher {
        return if (hasUserName) {
            object : UserInfoBridgeDispatcher {
                override fun getUserName(
                    onSuccess: (userName: String) -> Unit,
                    onError: (message: String) -> Unit
                ) {
                    if (canGetName)
                        onSuccess.invoke(TEST_USER_NAME)
                    else
                        onError.invoke(TEST_ERROR_MSG)
                }
            }
        } else {
            object : UserInfoBridgeDispatcher {}
        }
    }

    private fun createUserInfoBridgeWrapper(userInfoBridgeDispatcher: UserInfoBridgeDispatcher): UserInfoBridge {
        val userInfoBridgeWrapper = UserInfoBridge()
        userInfoBridgeWrapper.setMiniAppComponents(
            bridgeExecutor,
            customPermissionCache,
            downloadedManifestCache,
            TEST_MA.id
        )
        userInfoBridgeWrapper.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)
        return userInfoBridgeWrapper
    }

    @Test
    fun `postError should be called when there is no UserInfoBridgeDispatcher`() {
        val errMsg = "The `UserInfoBridgeDispatcher` ${ErrorBridgeMessage.NO_IMPL}"
        // not set UserInfoBridgeDispatcher.
        miniAppBridge.postMessage(Gson().toJson(userNameCallbackObj))
        // not call init for MiniAppMessageBridge.
        val miniAppBridgeSecond = Mockito.spy(createMessageBridge())
        miniAppBridgeSecond.setUserInfoBridgeDispatcher(createUserNameImpl(false, false))
        miniAppBridgeSecond.postMessage(Gson().toJson(userNameCallbackObj))

        verify(bridgeExecutor).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when there is no get user name retrieval implementation`() {
        val userInfoBridgeDispatcher = Mockito.spy(createUserNameImpl(false, false))
        miniAppBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)
        val errMsg = "$ERR_GET_USER_NAME The `UserInfoBridgeDispatcher.getUserName` ${ErrorBridgeMessage.NO_IMPL}"
        miniAppBridge.postMessage(Gson().toJson(userNameCallbackObj))

        verify(bridgeExecutor).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when user name permission hasn't been allowed`() {
        val userInfoBridgeDispatcher = Mockito.spy(createUserNameImpl(true, false))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        val errMsg = "$ERR_GET_USER_NAME Permission has not been accepted yet for getting user name."
        whenever(customPermissionCache.hasPermission(
            miniAppInfo.id, MiniAppCustomPermissionType.USER_NAME)
        ).thenReturn(false)

        userInfoBridgeWrapper.onGetUserName(userNameCallbackObj.id)

        verify(bridgeExecutor).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when onGetUserName failed`() {
        val userInfoBridgeDispatcher = Mockito.spy(createUserNameImpl(true, false))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        val errMsg = "Cannot get user name: error_message"

        userInfoBridgeWrapper.onGetUserName(userNameCallbackObj.id)

        verify(bridgeExecutor).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when onGetUserName retrieve valid user name`() {
        val userInfoBridgeDispatcher = Mockito.spy(createUserNameImpl(true, true))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        userInfoBridgeWrapper.onGetUserName(userNameCallbackObj.id)

        verify(bridgeExecutor, times(1)).postValue(userNameCallbackObj.id, TEST_USER_NAME)
    }
    /** end region */

    /** start region: onGetProfilePhoto */
    private fun createProfilePhotoImpl(
        hasProfilePhoto: Boolean,
        canGetPhoto: Boolean
    ): UserInfoBridgeDispatcher {
        return if (hasProfilePhoto) {
            object : UserInfoBridgeDispatcher {
                override fun getProfilePhoto(
                    onSuccess: (profilePhoto: String) -> Unit,
                    onError: (message: String) -> Unit
                ) {
                    if (canGetPhoto)
                        onSuccess.invoke(TEST_PROFILE_PHOTO)
                    else
                        onError.invoke(TEST_ERROR_MSG)
                }
            }
        } else {
            object : UserInfoBridgeDispatcher {}
        }
    }

    @Test
    fun `postError should be called when there is no get profile photo retrieval implementation`() {
        val userInfoBridgeDispatcher = Mockito.spy(createProfilePhotoImpl(false, false))
        miniAppBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)
        val errMsg = "$ERR_GET_PROFILE_PHOTO The `UserInfoBridgeDispatcher.getProfilePhoto` " +
                ErrorBridgeMessage.NO_IMPL
        miniAppBridge.postMessage(Gson().toJson(profilePhotoCallbackObj))

        verify(bridgeExecutor).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when profile photo permission hasn't been allowed`() {
        val userInfoBridgeDispatcher = Mockito.spy(createProfilePhotoImpl(true, false))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        val errMsg = "$ERR_GET_PROFILE_PHOTO Permission has not been accepted yet for getting profile photo."
        whenever(customPermissionCache.hasPermission(
            miniAppInfo.id, MiniAppCustomPermissionType.PROFILE_PHOTO)
        ).thenReturn(false)

        userInfoBridgeWrapper.onGetProfilePhoto(profilePhotoCallbackObj.id)

        verify(bridgeExecutor).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when onGetProfilePhoto failed`() {
        val userInfoBridgeDispatcher = Mockito.spy(createProfilePhotoImpl(true, false))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        val errMsg = "Cannot get profile photo: error_message"

        userInfoBridgeWrapper.onGetProfilePhoto(profilePhotoCallbackObj.id)

        verify(bridgeExecutor).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when onGetProfilePhoto retrieve valid profile photo url`() {
        val userInfoBridgeDispatcher = Mockito.spy(createProfilePhotoImpl(true, true))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        userInfoBridgeWrapper.onGetProfilePhoto(profilePhotoCallbackObj.id)

        verify(bridgeExecutor, times(1)).postValue(profilePhotoCallbackObj.id, TEST_PROFILE_PHOTO)
    }
    /** end region */

    /** start region: access token */
    private val testToken = TokenData("test_token", 0)
    private val tokenCallbackObj = CallbackObj(
            action = ActionType.GET_ACCESS_TOKEN.action,
            param = Gson().toJson(TEST_ATP1),
            id = TEST_CALLBACK_ID
    )

    private fun createAccessTokenImpl(
        hasAccessToken: Boolean,
        canGetToken: Boolean
    ): UserInfoBridgeDispatcher {
        return if (hasAccessToken) {
            object : UserInfoBridgeDispatcher {
                override fun getAccessToken(
                    miniAppId: String,
                    accessTokenScope: AccessTokenScope,
                    onSuccess: (tokenData: TokenData) -> Unit,
                    onError: (message: String) -> Unit
                ) {
                    if (canGetToken)
                        onSuccess.invoke(testToken)
                    else
                        onError.invoke(TEST_ERROR_MSG)
                }
            }
        } else {
            object : UserInfoBridgeDispatcher {}
        }
    }

    @Test
    fun `postError should be called when there is no access token retrieval implementation`() {
        val userInfoBridgeDispatcher = Mockito.spy(createAccessTokenImpl(false, false))
        miniAppBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)
        val errMsg = "$ERR_GET_ACCESS_TOKEN The `UserInfoBridgeDispatcher.getAccessToken` ${ErrorBridgeMessage.NO_IMPL}"
        miniAppBridge.postMessage(Gson().toJson(tokenCallbackObj))

        verify(bridgeExecutor).postError(tokenCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp denies providing access token`() {
        val errMsg = "$ERR_GET_ACCESS_TOKEN $TEST_ERROR_MSG"
        val userInfoBridgeDispatcher = Mockito.spy(createAccessTokenImpl(true, false))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        userInfoBridgeWrapper.onGetAccessToken(tokenCallbackObj)

        verify(bridgeExecutor).postError(tokenCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when access token permission hasn't been allowed`() {
        val userInfoBridgeDispatcher = Mockito.spy(createAccessTokenImpl(true, true))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        val errMsg = "$ERR_GET_ACCESS_TOKEN $ERR_ACCESS_TOKEN_NO_PERMISSION"
        whenever(customPermissionCache.hasPermission(
            miniAppInfo.id, MiniAppCustomPermissionType.ACCESS_TOKEN)
        ).thenReturn(false)

        userInfoBridgeWrapper.onGetAccessToken(tokenCallbackObj)

        verify(bridgeExecutor).postError(tokenCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when the requested audience and scope not match the define in manifest`() {
        val userInfoBridgeDispatcher = Mockito.spy(createAccessTokenImpl(true, true))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))
        val errMsg = "$ERR_GET_ACCESS_TOKEN $ERR_ACCESS_TOKEN_NOT_MATCH_MANIFEST"

        val tokenCallbackObj = CallbackObj(
            action = ActionType.GET_ACCESS_TOKEN.action,
            param = null,
            id = TEST_CALLBACK_ID
        )
        userInfoBridgeWrapper.onGetAccessToken(tokenCallbackObj)
        verify(bridgeExecutor).postError(tokenCallbackObj.id, errMsg)

        val atp2 = AccessTokenScope(audience = "aud", scopes = mutableListOf("scopeB"))
        val tokenCallbackObj2 = CallbackObj(
            action = ActionType.GET_ACCESS_TOKEN.action,
            param = Gson().toJson(atp2),
            id = TEST_CALLBACK_ID + '2'
        )
        userInfoBridgeWrapper.onGetAccessToken(tokenCallbackObj2)
        verify(bridgeExecutor).postError(tokenCallbackObj2.id, errMsg)

        val atp3 = AccessTokenScope(audience = "aud1", scopes = mutableListOf())
        val tokenCallbackObj3 = CallbackObj(
            action = ActionType.GET_ACCESS_TOKEN.action,
            param = Gson().toJson(atp3),
            id = TEST_CALLBACK_ID + '3'
        )
        userInfoBridgeWrapper.onGetAccessToken(tokenCallbackObj3)
        verify(bridgeExecutor).postError(tokenCallbackObj3.id, errMsg)

        val atp4 = AccessTokenScope(audience = "aud2", scopes = mutableListOf("scopeA", "scopeB"))
        val tokenCallbackObj4 = CallbackObj(
            action = ActionType.GET_ACCESS_TOKEN.action,
            param = Gson().toJson(atp4),
            id = TEST_CALLBACK_ID + '4'
        )
        userInfoBridgeWrapper.onGetAccessToken(tokenCallbackObj4)
        verify(bridgeExecutor).postError(tokenCallbackObj4.id, errMsg)
    }

    @Test
    fun `should return the audience & scopes for miniapp`() {
        val userInfoBridgeDispatcher = Mockito.spy(createAccessTokenImpl(true, true))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        val atp2 = AccessTokenScope(audience = "aud1", scopes = mutableListOf("scopeB"))
        val tokenCallbackObj2 = CallbackObj(
            action = ActionType.GET_ACCESS_TOKEN.action,
            param = Gson().toJson(atp2),
            id = TEST_CALLBACK_ID
        )
        userInfoBridgeWrapper.onGetAccessToken(tokenCallbackObj2)

        verify(bridgeExecutor).postValue(tokenCallbackObj2.id, Gson().toJson(testToken))
        testToken.scopes shouldEqual atp2
    }

    @Test
    fun `postValue should be called when retrieve access token successfully`() {
        val userInfoBridgeDispatcher = Mockito.spy(createAccessTokenImpl(true, true))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        userInfoBridgeWrapper.onGetAccessToken(tokenCallbackObj)
        verify(bridgeExecutor).postValue(tokenCallbackObj.id, Gson().toJson(testToken))
    }

    @Test
    fun `will call deprecated function to get token when still using old implementation`() {
        val userInfoBridgeDispatcher = Mockito.spy(object : UserInfoBridgeDispatcher {
            override fun getAccessToken(
                miniAppId: String,
                onSuccess: (tokenData: TokenData) -> Unit,
                onError: (message: String) -> Unit
            ) {
                onSuccess.invoke(testToken)
            }
        })
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        userInfoBridgeWrapper.onGetAccessToken(tokenCallbackObj)

        verify(bridgeExecutor).postValue(tokenCallbackObj.id, Gson().toJson(testToken))
    }
    /** end region */

    /** start region: get contacts */
    private val contacts = arrayListOf(TEST_CONTACT)
    private fun createContactsImpl(
        hasGetContacts: Boolean,
        canGetContacts: Boolean
    ): UserInfoBridgeDispatcher {
        return if (hasGetContacts) {
            object : UserInfoBridgeDispatcher {
                override fun getContacts(
                    onSuccess: (contacts: ArrayList<Contact>) -> Unit,
                    onError: (message: String) -> Unit
                ) {
                    if (canGetContacts)
                        onSuccess.invoke(contacts)
                    else
                        onError.invoke(TEST_ERROR_MSG)
                }
            }
        } else {
            object : UserInfoBridgeDispatcher {}
        }
    }

    @Test
    fun `postError should be called when there is no get contacts retrieval implementation`() {
        val userInfoBridgeDispatcher = Mockito.spy(createContactsImpl(false, false))
        miniAppBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)
        val errMsg = "$ERR_GET_CONTACTS The `UserInfoBridgeDispatcher.getContacts` ${ErrorBridgeMessage.NO_IMPL}"
        miniAppBridge.postMessage(Gson().toJson(contactsCallbackObj))

        verify(bridgeExecutor).postError(contactsCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when contact permission hasn't been allowed`() {
        val userInfoBridgeDispatcher = Mockito.spy(createContactsImpl(true, true))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        val errMsg = "$ERR_GET_CONTACTS $ERR_GET_CONTACTS_NO_PERMISSION"
        whenever(customPermissionCache.hasPermission(
            miniAppInfo.id, MiniAppCustomPermissionType.CONTACT_LIST)
        ).thenReturn(false)

        userInfoBridgeWrapper.onGetContacts(contactsCallbackObj.id)

        verify(bridgeExecutor).postError(contactsCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp doesn't providing contacts`() {
        val errMsg = "$ERR_GET_CONTACTS $TEST_ERROR_MSG"
        val userInfoBridgeDispatcher = Mockito.spy(createContactsImpl(true, false))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        userInfoBridgeWrapper.onGetContacts(contactsCallbackObj.id)

        verify(bridgeExecutor).postError(contactsCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when retrieve contacts successfully`() {
        val userInfoBridgeDispatcher = Mockito.spy(createContactsImpl(true, true))
        val userInfoBridgeWrapper = Mockito.spy(createUserInfoBridgeWrapper(userInfoBridgeDispatcher))

        userInfoBridgeWrapper.onGetContacts(contactsCallbackObj.id)

        verify(bridgeExecutor).postValue(contactsCallbackObj.id, Gson().toJson(contacts))
    }
    /** end region */

    private fun createMessageBridge(): MiniAppMessageBridge =
        object : MiniAppMessageBridge() {

            override fun getUniqueId() = TEST_CALLBACK_VALUE

            override fun requestDevicePermission(
                miniAppPermissionType: MiniAppDevicePermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) {
                onRequestDevicePermissionsResult(TEST_CALLBACK_ID, false)
            }
        }
}
