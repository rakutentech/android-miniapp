package com.rakuten.tech.mobile.testapp

import com.rakuten.tech.mobile.testapp.ui.input.MiniAppInputActivity
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuActivity

object AppScreen {
    val MINI_APP_LIST_ACTIVITY = MiniAppListActivity::class.java.canonicalName
    val MINI_APP_INPUT_ACTIVITY = MiniAppInputActivity::class.java.canonicalName
    val MINI_APP_SETTINGS_ACTIVITY = SettingsMenuActivity::class.java.canonicalName
}
