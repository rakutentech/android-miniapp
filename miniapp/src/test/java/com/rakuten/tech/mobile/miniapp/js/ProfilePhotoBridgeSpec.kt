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
class ProfilePhotoBridgeSpec : BridgeCommon() {
    private val profilePhotoCallbackObj = CallbackObj(
        action = ActionType.GET_PROFILE_PHOTO.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    private val profilePhotoJsonStr = Gson().toJson(profilePhotoCallbackObj)
    private val customPermissionCache: MiniAppCustomPermissionCache = mock()
    private val miniAppInfo = MiniAppInfo(
        id = TEST_MA_ID,
        displayName = TEST_MA_DISPLAY_NAME,
        icon = TEST_MA_ICON,
        version = Version(TEST_MA_VERSION_TAG, TEST_MA_VERSION_ID)
    )
    private val allowedProfilePhotoPermission = MiniAppCustomPermission(
        TEST_MA_ID,
        listOf(
            Pair(
                MiniAppCustomPermissionType.PROFILE_PHOTO,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )
    )

    @Before
    fun setupAllowedProfilePhoto() {
        whenever(customPermissionCache.readPermissions(miniAppInfo.id)).thenReturn(
            allowedProfilePhotoPermission
        )
    }

    @Test
    fun `postError should be called when cannot get profile photo`() {
        val errMsg = "Cannot get profile photo: null"
        val miniAppBridge = Mockito.spy(createDefaultMiniAppMessageBridge())
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = mock(),
            customPermissionCache = mock(),
            miniAppInfo = mock()
        )
        miniAppBridge.postMessage(profilePhotoJsonStr)

        verify(miniAppBridge).postError(profilePhotoCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when getProfilePhoto hasn't been implemented`() {
        val errMsg = "Cannot get profile photo: The `MiniAppMessageBridge.getProfilePhoto`" +
                " method has not been implemented by the Host App."
        val miniAppBridge = Mockito.spy(createMiniAppMessageBridge(false))
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = mock(),
            customPermissionCache = customPermissionCache,
            miniAppInfo = miniAppInfo
        )
        miniAppBridge.postMessage(profilePhotoJsonStr)

        verify(miniAppBridge).postError(profilePhotoCallbackObj.id, errMsg)
    }
}
