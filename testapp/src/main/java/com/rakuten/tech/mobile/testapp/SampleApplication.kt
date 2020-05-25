package com.rakuten.tech.mobile.testapp

import android.app.Application
import com.rakuten.tech.mobile.testapp.helper.MiniAppListHelper
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        AppSettings.init(this)
        MiniAppListHelper.init(this)
    }
}
