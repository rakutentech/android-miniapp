package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
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

const val GET_UNIQUE_ID = "getUniqueId"

@RunWith(AndroidJUnit4::class)
class MiniAppMessageBridgeSpec {
    private val miniAppBridge: MiniAppMessageBridge = Mockito.spy(
        object : MiniAppMessageBridge(ApplicationProvider.getApplicationContext()) {
            override fun getUniqueId() = TEST_CALLBACK_VALUE
        }
    )
    private val callbackObj = CallbackObj(GET_UNIQUE_ID, TEST_CALLBACK_ID)
    private val jsonStr = Gson().toJson(callbackObj)

    @Before
    fun setup() {
        miniAppBridge.setWebViewListener(mock())
    }

    @Test
    fun `getUniqueId should be called when there is a getting unique id request from external`() {
        miniAppBridge.postMessage(jsonStr)

        verify(miniAppBridge, times(1)).getUniqueId()
        verify(miniAppBridge, times(1)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Test
    fun `postValue should not be called when calling postError`() {
        miniAppBridge.postError(TEST_CALLBACK_ID, TEST_ERROR_MSG)

        verify(miniAppBridge, times(0)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }

    @Suppress("TooGenericExceptionThrown")
    @Test
    fun `postError should be called when error occurs in postValue`() {
        val errMsg = "Cannot get unique id: null"
        miniAppBridge.setWebViewListener(object : WebViewListener {
            override fun runSuccessCallback(callbackId: String, value: String) {
                throw Exception()
            }

            override fun runErrorCallback(callbackId: String, errorMessage: String) {
                Assert.assertEquals(errorMessage, errMsg)
            }

            override fun onRequestPermissionsResult(
                requestCode: Int,
                permission: String,
                grantResult: Int
            ) {
                throw Exception()
            }
        })
        miniAppBridge.postMessage(jsonStr)

        verify(miniAppBridge, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `should pass permission result to webview`() {
        val webViewListener: WebViewListener = spy()
        miniAppBridge.setWebViewListener(webViewListener)
        val requestCode = 0
        val permissions = arrayOf("")
        val grantResults = IntArray(1).also { it[0] = 0 }

        miniAppBridge.onRequestPermissionsResult(requestCode, permissions, grantResults)

        verify(webViewListener, times(1)).onRequestPermissionsResult(
            requestCode, permissions[0], grantResults[0])
    }
}
