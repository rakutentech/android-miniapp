package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.content.Intent
import android.os.Bundle
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity

/**
 * This activity will be the gateway of all deeplink scheme.
 */
class SchemeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let { data ->
            if (data.pathSegments.size > 2) {
                //token value to get miniAppInfo.
                val token = data.pathSegments[2]
            }

            startActivity(Intent(this, MiniAppListActivity::class.java))
            finish()
        }
    }
}
