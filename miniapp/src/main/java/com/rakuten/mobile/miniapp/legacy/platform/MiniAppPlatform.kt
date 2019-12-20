package com.rakuten.mobile.miniapp.legacy.platform

import android.app.Activity
import android.content.Context
import com.rakuten.mobile.miniapp.legacy.core.exceptions.MiniAppPlatformException

/**
 * Main interface which host app should interact with.
 */
interface MiniAppPlatform {

    /**
     * Initializing MiniApp SDK.
     */
    fun init(applicationContext: Context): MiniAppPlatform

    /**
     * Download the MiniApp based on manifest manifestEndpoint which shouldn't include base URL.
     */
    @Throws(MiniAppPlatformException::class)
    fun download(manifestEndpoint: String)

    /**
     * Display mini app based on mini app's ID and version. If mini app doesn't exist locally, it will
     * be downloaded first, then displayed.
     */
    fun displayMiniApp(miniAppId: String, versionId: String, hostActivity: Activity)

    /**
     * Turning on debug logging.
     */
    fun debugLogging(shouldLog: Boolean): MiniAppPlatform

    /**
     * Checks if MiniApp Platform has been initialized.
     */
    fun isInitialized(): Boolean
}
