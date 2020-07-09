package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.Intent
import android.view.*
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity

abstract class MenuBaseActivity : BaseActivity() {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.set_app_id_and_key -> navigateToSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToSettings(): Boolean {
        startActivity(Intent(this, SettingsMenuActivity::class.java))
        return true
    }
}
