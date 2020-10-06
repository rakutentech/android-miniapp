package com.rakuten.tech.mobile.miniapp.js

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_DISPLAY_NAME
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@Suppress("LongMethod")
@RunWith(AndroidJUnit4::class)
class UserNameBridgeSpec : BridgeCommon() {
    private val userNameCallbackObj = CallbackObj(
        action = ActionType.GET_USER_NAME.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    private val userNameJsonStr = Gson().toJson(userNameCallbackObj)
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
    fun setupAllowedUserName() {
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            allowedUserNamePermission
        )
    }

    @Test
    fun `postError should be called when cannot get user name`() {
        val errMsg = "Cannot get user name: null"
        val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = mock(),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        miniAppBridge.postMessage(userNameJsonStr)

        verify(miniAppBridge).postError(userNameCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when getUserName hasn't been implemented`() {
        val errMsg = "Cannot get user name: The `MiniAppMessageBridge.getUserName`" +
                " method has not been implemented by the Host App."
        val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(false))
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = mock(),
            customPermissionCache = customPermissionCache,
            miniAppInfo = miniAppInfo
        )
        miniAppBridge.postMessage(userNameJsonStr)

        verify(miniAppBridge).postError(userNameCallbackObj.id, errMsg)
    }
}
