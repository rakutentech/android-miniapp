package com.rakuten.tech.mobile.testapp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.crashes.Crashes
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize demo app's settings properties
        AppSettings.init(this)
        // Enable AdMob
        MobileAds.initialize(this)

        // Enable microsoft's AppCenter Crash class for staging, rc and release builds
        if (BuildConfig.ENABLE_APPCENTER_CRASHLYTICS)
            AppCenter.start(this, getString(R.string.appcenter_secret), Crashes::class.java)
    }
}
