package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_DISPLAY_NAME
import com.rakuten.tech.mobile.miniapp.TEST_MA_ICON
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_TAG
import com.rakuten.tech.mobile.miniapp.TEST_USER_NAME
import com.rakuten.tech.mobile.miniapp.js.ActionType
import com.rakuten.tech.mobile.miniapp.js.BridgeCommon
import com.rakuten.tech.mobile.miniapp.js.CallbackObj
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@Suppress("LongMethod")
@RunWith(AndroidJUnit4::class)
class UserInfoHandlerSpec : BridgeCommon() {
    private lateinit var userInfoHandler: UserInfoHandler
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
    private val customPermissionCache: MiniAppCustomPermissionCache = mock()
    private val miniAppInfo = MiniAppInfo(
        id = TEST_MA_ID,
        displayName = TEST_MA_DISPLAY_NAME,
        icon = TEST_MA_ICON,
        version = Version(TEST_MA_VERSION_TAG, TEST_MA_VERSION_ID)
    )
    private val userInfoAllowedPermission = MiniAppCustomPermission(
        TEST_MA_ID,
        listOf(
            Pair(
                MiniAppCustomPermissionType.USER_NAME,
                MiniAppCustomPermissionResult.ALLOWED
            ),
            Pair(
                MiniAppCustomPermissionType.PROFILE_PHOTO,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )
    )

    @Before
    fun setup() {
        miniAppBridge = Mockito.spy(createMessageBridge())
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = mock(),
            customPermissionCache = customPermissionCache,
            miniAppInfo = miniAppInfo
        )
        userInfoHandler = Mockito.spy(createUserInfoHandler())
        miniAppBridge.setUserInfoHandler(userInfoHandler)
    }

    /** start region: onGetUserName */
    @Test
    fun `postError should be called when cannot get user name`() {
        val errMsg = "Cannot get user name: null"
        miniAppBridge.postMessage(Gson().toJson(userNameCallbackObj))

        verify(miniAppBridge).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when user name permission hasn't been allowed`() {
        val errMsg =
            "Cannot get user name: Permission has not been accepted yet for getting user name."
        val deniedUserNamePermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(
                Pair(
                    MiniAppCustomPermissionType.USER_NAME,
                    MiniAppCustomPermissionResult.DENIED
                )
            )
        )
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            deniedUserNamePermission
        )

        userInfoHandler.onGetUserName(userNameCallbackObj.id)

        verify(miniAppBridge, times(1)).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when user name is empty`() {
        val errMsg = "Cannot get user name: User name is not found."
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            userInfoAllowedPermission
        )

        userInfoHandler.onGetUserName(userNameCallbackObj.id)

        verify(miniAppBridge, times(1)).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when onGetUserName retrieve valid user name`() {
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            userInfoAllowedPermission
        )
        whenever(userInfoHandler.getUserName()).thenReturn(TEST_USER_NAME)

        userInfoHandler.onGetUserName(userNameCallbackObj.id)

        verify(miniAppBridge, times(1)).postValue(userNameCallbackObj.id, TEST_USER_NAME)
    }
    /** end region */

    /** start region: onGetProfilePhoto */
    @Test
    fun `postError should be called when cannot get profile photo`() {
        val errMsg = "Cannot get profile photo: null"
        miniAppBridge.postMessage(Gson().toJson(profilePhotoCallbackObj))

        verify(miniAppBridge).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when profile photo permission hasn't been allowed`() {
        val errMsg =
            "Cannot get profile photo: Permission has not been accepted yet for getting profile photo."
        val deniedProfilePhotoPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(
                Pair(
                    MiniAppCustomPermissionType.PROFILE_PHOTO,
                    MiniAppCustomPermissionResult.DENIED
                )
            )
        )
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            deniedProfilePhotoPermission
        )

        userInfoHandler.onGetProfilePhoto(profilePhotoCallbackObj.id)

        verify(miniAppBridge, times(1)).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when profile photo is empty`() {
        val errMsg = "Cannot get profile photo: Profile photo is not found."
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            userInfoAllowedPermission
        )

        userInfoHandler.onGetProfilePhoto(profilePhotoCallbackObj.id)

        verify(miniAppBridge, times(1)).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when onGetProfilePhoto retrieve valid profile photo url`() {
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            userInfoAllowedPermission
        )
        whenever(userInfoHandler.getProfilePhoto()).thenReturn(TEST_PROFILE_PHOTO)

        userInfoHandler.onGetProfilePhoto(profilePhotoCallbackObj.id)

        verify(miniAppBridge, times(1)).postValue(profilePhotoCallbackObj.id, TEST_PROFILE_PHOTO)
    }

    /** end region */

    private fun createMessageBridge(): MiniAppMessageBridge =
        object : MiniAppMessageBridge() {

            override fun getUniqueId() = TEST_CALLBACK_VALUE

            override fun requestPermission(
                miniAppPermissionType: MiniAppPermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) {
                onRequestPermissionsResult(TEST_CALLBACK_ID, false)
            }
        }

    private fun createUserInfoHandler(): UserInfoHandler =
        object : UserInfoHandler() {
            override fun getUserName() = ""

            override fun getProfilePhoto() = ""
        }
}
