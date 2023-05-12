package com.rakuten.tech.mobile.miniapp.js

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import org.junit.Test
import org.mockito.kotlin.mock

class UnimplementedMessageBridgeSpec : BridgeCommon() {

    private val unimplementedMessageBridge = object : MiniAppMessageBridge() {}

    @Test(expected = MiniAppSdkException::class)
    fun `getMessaginguniqueId should throw MiniAppSdkException when it is not implemented`() {
        unimplementedMessageBridge.getMessagingUniqueId(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `getMauid should throw MiniAppSdkException when it is not implemented`() {
        unimplementedMessageBridge.getMauid(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `requestDevicepPermission throw MiniAppSdkException when it is not implemented`() {
        unimplementedMessageBridge.requestDevicePermission(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `requestCustomPermissions throw MiniAppSdkException when it is not implemented`() {
        unimplementedMessageBridge.requestCustomPermissions(mock(), mock())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `sendJsonToHostApp throw MiniAppSdkException when it is not implemented`() {
        val mockFunction: (Any) -> Unit = {}
        unimplementedMessageBridge.sendJsonToHostApp("", mockFunction, mockFunction)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `getHostAppThemeColors should throw MiniAppSdkException when it is not implemented`() {
        unimplementedMessageBridge.getHostAppThemeColors(mock(), mock())
    }
}
