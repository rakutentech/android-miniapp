package com.rakuten.tech.mobile.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListFragment

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
