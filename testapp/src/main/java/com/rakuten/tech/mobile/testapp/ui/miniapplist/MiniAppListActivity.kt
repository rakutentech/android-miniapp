package com.rakuten.tech.mobile.testapp.ui.miniapplist

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.View
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuBaseActivity
import kotlinx.android.synthetic.main.mini_app_list_activity.*

class MiniAppListActivity : SettingsMenuBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mini_app_list_activity)
        if (AppSettings.instance.isSettingSaved()) {
            layoutTut.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MiniAppListFragment.newInstance())
                .commitNow()
        } else {
            layoutTut.visibility = View.VISIBLE
            val tutString: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(getString(R.string.tut_setting), FROM_HTML_MODE_LEGACY)
            } else
                Html.fromHtml(getString(R.string.tut_setting))
            tvTut.text = tutString
        }
    }

}
