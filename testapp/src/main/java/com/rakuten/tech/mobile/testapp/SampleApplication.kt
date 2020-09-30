package com.rakuten.tech.mobile.testapp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.rakuten.tech.mobile.testapp.helper.MiniAppListStore
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        AppSettings.init(this)
        MiniAppListStore.init(this)
        // Enable AdMob
        MobileAds.initialize(this)
    }
}
