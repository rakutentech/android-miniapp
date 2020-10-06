package com.rakuten.tech.mobile.miniapp.userinfo

import androidx.test.ext.junit.runners.AndroidJUnit4
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
import com.rakuten.tech.mobile.miniapp.js.CallbackObj
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@Suppress("LongMethod")
@RunWith(AndroidJUnit4::class)
class UserInfoHandlerSpec {
    private lateinit var userInfoHandler: UserInfoHandler
    private lateinit var miniAppBridge: MiniAppMessageBridge
    private val userNameCallbackObj = CallbackObj(
        action = ActionType.GET_USER_NAME.action,
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
    private val allowedUserNamePermission = MiniAppCustomPermission(
        TEST_MA_ID,
        listOf(
            Pair(
                MiniAppCustomPermissionType.USER_NAME,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )
    )

    @Before
    fun setup() {
        miniAppBridge = Mockito.spy(createUserInfoMessageBridge())
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = mock(),
            customPermissionCache = customPermissionCache,
            miniAppInfo = miniAppInfo
        )
        userInfoHandler = UserInfoHandler(miniAppBridge)
    }

    @Test
    fun `postError should be called when user name permission hasn't been allowed`() {
        val errMsg = "Cannot get user name: Permission has not been accepted yet for getting user name."
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

        userInfoHandler.onGetUserName(userNameCallbackObj)

        verify(miniAppBridge, times(1)).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when user name is empty`() {
        val errMsg = "Cannot get user name: User name is not found."
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            allowedUserNamePermission
        )

        userInfoHandler.onGetUserName(userNameCallbackObj)

        verify(miniAppBridge, times(1)).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when onGetUserName retrieve valid user name`() {
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            allowedUserNamePermission
        )
        whenever(miniAppBridge.getUserName()).thenReturn(TEST_USER_NAME)

        userInfoHandler.onGetUserName(userNameCallbackObj)

        verify(miniAppBridge, times(1)).postValue(userNameCallbackObj.id, TEST_USER_NAME)
    }

    private fun createUserInfoMessageBridge(): MiniAppMessageBridge =
        object : MiniAppMessageBridge() {

            override fun getUniqueId() = TEST_CALLBACK_VALUE

            override fun requestPermission(
                miniAppPermissionType: MiniAppPermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) {
                onRequestPermissionsResult(TEST_CALLBACK_ID, false)
            }

            override fun getUserName() = ""
        }
}
