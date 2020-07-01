package com.rakuten.tech.mobile.miniapp.js

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_VALUE
import com.rakuten.tech.mobile.miniapp.TEST_ERROR_MSG
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@Suppress("TooGenericExceptionThrown")
@RunWith(AndroidJUnit4::class)
class MiniAppMessageBridgeSpec {
    private val miniAppBridge: MiniAppMessageBridge = Mockito.spy(
        object : MiniAppMessageBridge() {
            override fun getUniqueId() = TEST_CALLBACK_VALUE

            override fun requestPermission(
                callbackId: String,
                miniAppPermissionType: String,
                permissions: Array<String>
            ) {
                onRequestPermissionsResult(TEST_CALLBACK_ID, 0)
            }
        }
    )
    private val uniqueIdCallbackObj = CallbackObj(
        action = ActionType.GET_UNIQUE_ID.action,
        param = null,
        id = TEST_CALLBACK_ID)
    private val uniqueIdJsonStr = Gson().toJson(uniqueIdCallbackObj)

    private val permissionCallbackObj = CallbackObj(
        action = ActionType.REQUEST_PERMISSION.action,
        param = Gson().toJson(Permission(MiniAppPermission.PermissionType.GEOLOCATION)),
        id = TEST_CALLBACK_ID)
    private val permissionJsonStr = Gson().toJson(permissionCallbackObj)

    private fun createErrorWebViewListener(errMsg: String): WebViewListener =
        object : WebViewListener {
            override fun runSuccessCallback(callbackId: String, value: String) {
                throw Exception()
            }

            override fun runErrorCallback(callbackId: String, errorMessage: String) {
                Assert.assertEquals(errorMessage, errMsg)
            }
        }

    @Before
    fun setup() {
        miniAppBridge.setWebViewListener(mock())
    }

    @Test
    fun `getUniqueId should be called when there is a getting unique id request from external`() {
        miniAppBridge.postMessage(uniqueIdJsonStr)

        verify(miniAppBridge, times(1)).getUniqueId()
        verify(miniAppBridge, times(1)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `requestPermission should be called when there is a permission request from external`() {
        val permissionParam = Gson().fromJson(permissionCallbackObj.param as String, Permission::class.java)

        miniAppBridge.postMessage(permissionJsonStr)

        verify(miniAppBridge, times(1)).requestPermission(
            callbackId = permissionCallbackObj.id,
            miniAppPermissionType = permissionParam.permission,
            permissions = MiniAppPermission.getPermissionRequest(permissionParam.permission))
    }

    @Test
    fun `postValue should not be called when calling postError`() {
        miniAppBridge.postError(TEST_CALLBACK_ID, TEST_ERROR_MSG)

        verify(miniAppBridge, times(0)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `postError should be called when cannot get unique id`() {
        val errMsg = "Cannot get unique id: null"
        miniAppBridge.setWebViewListener(createErrorWebViewListener(errMsg))
        miniAppBridge.postMessage(uniqueIdJsonStr)

        verify(miniAppBridge, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called when cannot request permission`() {
        val errMsg = "Cannot request permission: null"
        miniAppBridge.setWebViewListener(createErrorWebViewListener(errMsg))
        miniAppBridge.postMessage(permissionJsonStr)

        verify(miniAppBridge, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }
}
