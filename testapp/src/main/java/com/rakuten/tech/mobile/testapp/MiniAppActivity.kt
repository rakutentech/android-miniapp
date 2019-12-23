package com.rakuten.tech.mobile.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rakuten.tech.mobile.miniapp.legacy.platform.MiniAppPlatformImpl
import com.rakuten.tech.mobile.testapp.MainActivity.Companion.APP_ID
import com.rakuten.tech.mobile.testapp.MainActivity.Companion.VERSION_ID

/**
 * Mini App activity.
 */
class MiniAppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MiniAppPlatformImpl()
            .displayMiniApp(APP_ID, VERSION_ID, this)
    }

}
