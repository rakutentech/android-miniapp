package com.rakuten.tech.mobile.testapp.ui.miniapplist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rakuten.tech.mobile.miniapp.testapp.R

class MiniAppListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mini_app_list_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MiniAppListFragment.newInstance())
                .commitNow()
        }
    }

}
