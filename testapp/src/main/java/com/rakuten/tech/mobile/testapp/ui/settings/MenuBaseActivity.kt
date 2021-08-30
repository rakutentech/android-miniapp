package com.rakuten.tech.mobile.testapp.ui.settings

import android.view.Menu
import android.view.MenuItem
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.analytics.rat_wrapper.RATActivity
import com.rakuten.tech.mobile.testapp.helper.RaceExecutor

abstract class MenuBaseActivity : RATActivity(), SettingsNavigator {

    val raceExecutor = RaceExecutor()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.set_app_id_and_key -> navigateToScreen()
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val MENU_SCREEN_NAME = "menu_screen_name"
    }
}
