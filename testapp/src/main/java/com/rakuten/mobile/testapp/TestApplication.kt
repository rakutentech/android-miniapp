package com.rakuten.mobile.testapp

import android.app.Application
import com.rakuten.mobile.miniapp.legacy.platform.MiniAppPlatformImpl

/**
 * Test app's application class.
 */
class TestApplication : Application() {

    /**
     * OnResume callback from Android system.
     */
    override fun onCreate() {
        super.onCreate()
        MiniAppPlatformImpl().debugLogging(true).init(this)
    }
}
