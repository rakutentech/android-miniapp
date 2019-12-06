package com.rakuten.mobile.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rakuten.mobile.miniapp.platform.MiniAppPlatformImpl
import com.rakuten.mobile.testapp.MainActivity.Companion.APP_ID
import com.rakuten.mobile.testapp.MainActivity.Companion.VERSION_ID

/**
 * Mini App activity.
 */
class MiniAppActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    MiniAppPlatformImpl().displayMiniApp(APP_ID, VERSION_ID, this)
  }

}
