package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
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
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.ActionType
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.js.CallbackObj
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridgeDispatcher.Companion.ERR_GET_ACCESS_TOKEN
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridgeDispatcher.Companion.ERR_GET_PROFILE_PHOTO
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridgeDispatcher.Companion.ERR_GET_USER_NAME
import com.rakuten.tech.mobile.miniapp.permission.*
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@Suppress("LongMethod", "LargeClass")
@RunWith(AndroidJUnit4::class)
class UserInfoBridgeDispatcherSpec {
    private lateinit var userInfoBridgeDispatcher: UserInfoBridgeDispatcher
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
    private val tokenCallbackObj = CallbackObj(
        action = ActionType.GET_ACCESS_TOKEN.action,
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
            miniAppId = TEST_MA.id
        )
        userInfoBridgeDispatcher = Mockito.spy(createUserInfoBridgeDispatcher())
        miniAppBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)
    }

    /** start region: onGetUserName */
    @Test
    fun `postError should be called when cannot get user name`() {
        val errMsg = "$ERR_GET_USER_NAME null"
        miniAppBridge.postMessage(Gson().toJson(userNameCallbackObj))

        verify(bridgeExecutor).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when user name permission hasn't been allowed`() {
        val errMsg = "$ERR_GET_USER_NAME Permission has not been accepted yet for getting user name."
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

        userInfoBridgeDispatcher.onGetUserName(userNameCallbackObj.id)

        verify(bridgeExecutor).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when user name is empty`() {
        val errMsg = "$ERR_GET_USER_NAME User name is not found."
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            userInfoAllowedPermission
        )

        userInfoBridgeDispatcher.onGetUserName(userNameCallbackObj.id)

        verify(bridgeExecutor).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when onGetUserName retrieve valid user name`() {
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            userInfoAllowedPermission
        )
        whenever(userInfoBridgeDispatcher.getUserName()).thenReturn(TEST_USER_NAME)

        userInfoBridgeDispatcher.onGetUserName(userNameCallbackObj.id)

        verify(bridgeExecutor).postValue(userNameCallbackObj.id, TEST_USER_NAME)
    }
    /** end region */

    /** start region: onGetProfilePhoto */
    @Test
    fun `postError should be called when cannot get profile photo`() {
        val errMsg = "$ERR_GET_PROFILE_PHOTO null"
        miniAppBridge.postMessage(Gson().toJson(profilePhotoCallbackObj))

        verify(bridgeExecutor).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when profile photo permission hasn't been allowed`() {
        val errMsg = "$ERR_GET_PROFILE_PHOTO Permission has not been accepted yet for getting profile photo."
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

        userInfoBridgeDispatcher.onGetProfilePhoto(profilePhotoCallbackObj.id)

        verify(bridgeExecutor).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when profile photo is empty`() {
        val errMsg = "$ERR_GET_PROFILE_PHOTO Profile photo is not found."
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            userInfoAllowedPermission
        )

        userInfoBridgeDispatcher.onGetProfilePhoto(profilePhotoCallbackObj.id)

        verify(bridgeExecutor).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when onGetProfilePhoto retrieve valid profile photo url`() {
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            userInfoAllowedPermission
        )
        whenever(userInfoBridgeDispatcher.getProfilePhoto()).thenReturn(TEST_PROFILE_PHOTO)

        userInfoBridgeDispatcher.onGetProfilePhoto(profilePhotoCallbackObj.id)

        verify(bridgeExecutor).postValue(profilePhotoCallbackObj.id, TEST_PROFILE_PHOTO)
    }

    /** end region */

    /** start region: access token */
    private val testToken = TokenData("test_token", 0)
    private fun createUserInfoImpl(canGetToken: Boolean) = object : UserInfoBridgeDispatcher() {
        override fun getAccessToken(
            miniAppId: String,
            onSuccess: (tokenData: TokenData) -> Unit,
            onError: (message: String) -> Unit
        ) {
            if (canGetToken)
                onSuccess.invoke(testToken)
            else
                onError.invoke(TEST_ERROR_MSG)
        }
    }

    @Test
    fun `postError should be called when there is no access token retrieval implementation`() {
        val errMsg = "$ERR_GET_ACCESS_TOKEN The `UserInfoBridgeDispatcher.getAccessToken`" +
                " method has not been implemented by the Host App."
        miniAppBridge.postMessage(Gson().toJson(tokenCallbackObj))

        verify(bridgeExecutor).postError(tokenCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp denies providing access token`() {
        val errMsg = "$ERR_GET_ACCESS_TOKEN $TEST_ERROR_MSG"
        val userInfoBridgeDispatcher = Mockito.spy(createUserInfoImpl(false))
        userInfoBridgeDispatcher.init(bridgeExecutor, customPermissionCache, TEST_MA_ID)

        userInfoBridgeDispatcher.onGetAccessToken(tokenCallbackObj.id)

        verify(bridgeExecutor).postError(tokenCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when retrieve access token successfully`() {
        val userInfoBridgeDispatcher = Mockito.spy(createUserInfoImpl(true))
        userInfoBridgeDispatcher.init(bridgeExecutor, customPermissionCache, TEST_MA_ID)

        userInfoBridgeDispatcher.onGetAccessToken(tokenCallbackObj.id)

        verify(bridgeExecutor).postValue(tokenCallbackObj.id, Gson().toJson(testToken))
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

    private fun createUserInfoBridgeDispatcher(): UserInfoBridgeDispatcher =
        object : UserInfoBridgeDispatcher() {
            override fun getUserName() = ""

            override fun getProfilePhoto() = ""
        }
}
