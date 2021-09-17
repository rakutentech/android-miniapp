package com.rakuten.tech.mobile.miniapp.js.hostenvironment

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL

/**
 * A class to provide the interface for providing host environment info.
 */
interface HostEnvironmentBridgeDispatcher {

    /**
     * Get environment info from host app.
     * You can also throw an [Exception] from this method to pass an error message to the mini app.
     */
    fun getHostEnvironmentInfo(
        onSuccess: (info: HostEnvironmentInfo) -> Unit,
        onError: (infoError: HostEnvironmentInfoError) -> Unit
    ) {
        throw MiniAppSdkException(NO_IMPL)
    }
}
