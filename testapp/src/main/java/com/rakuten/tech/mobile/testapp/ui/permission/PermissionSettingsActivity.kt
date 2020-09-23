package com.rakuten.tech.mobile.testapp.ui.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity

class PermissionSettingsActivity : BaseActivity() {

    companion object {
        private const val miniAppTag = "mini_app_tag"

        fun start(context: Context, miniAppInfo: MiniAppInfo) {
            context.startActivity(Intent(context, PermissionSettingsActivity::class.java).apply {
                putExtra(miniAppTag, miniAppInfo)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        setContentView(R.layout.permission_settings_activity)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
