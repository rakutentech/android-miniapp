package com.rakuten.tech.mobile.miniapp.js

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_VALUE
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

const val GET_UNIQUE_ID = "getUniqueId"

class MiniAppMessageBridgeSpec {
    private val miniAppBridge: MiniAppMessageBridge = Mockito.spy(object : MiniAppMessageBridge() {
        override fun getUniqueId(): String = TEST_CALLBACK_VALUE
    })
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
    }

    @Test
    fun `postValue should be called when there is a valid action callback`() {
        miniAppBridge.postMessage(jsonStr)

        verify(miniAppBridge, times(1)).postValue(TEST_CALLBACK_ID, TEST_CALLBACK_VALUE)
    }
}
