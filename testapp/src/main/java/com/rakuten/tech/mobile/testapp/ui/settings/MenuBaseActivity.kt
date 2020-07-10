package com.rakuten.tech.mobile.testapp.ui.settings

import android.view.Menu
import android.view.MenuItem
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity

abstract class MenuBaseActivity : BaseActivity(), SettingsNavigator {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.set_app_id_and_key -> navigateToScreen()
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val MENU_SCREEN_NAME = "menu_screen_name"
    }
}
