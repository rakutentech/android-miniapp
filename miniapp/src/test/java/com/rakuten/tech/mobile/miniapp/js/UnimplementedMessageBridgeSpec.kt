package com.rakuten.tech.mobile.miniapp.js

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import org.junit.Test
import org.mockito.kotlin.mock

class UnimplementedMessageBridgeSpec : BridgeCommon() {

    private val unImplementedMessageBrdige = object : MiniAppMessageBridge() {}

    @Test(expected = MiniAppSdkException::class)
    fun `getUniqueId should throw MiniAppSdkException when it is not implemented`() {
        unImplementedMessageBrdige.getUniqueId(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `getMessaginguniqueId should throw MiniAppSdkException when it is not implemented`() {
        unImplementedMessageBrdige.getMessagingUniqueId(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `getMauid should throw MiniAppSdkException when it is not implemented`() {
        unImplementedMessageBrdige.getMauid(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `requestDevicepPermission throw MiniAppSdkException when it is not implemented`() {
        unImplementedMessageBrdige.requestDevicePermission(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `requestCustomPermissions throw MiniAppSdkException when it is not implemented`() {
        unImplementedMessageBrdige.requestCustomPermissions(mock(), mock())
    }
}
