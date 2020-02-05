package com.rakuten.tech.mobile.testapp.ui.display

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.webkit.WebView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import kotlinx.coroutines.launch

class MiniAppDisplayActivity: BaseActivity() {

    private lateinit var viewModel: MiniAppDwnlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mini_app_display_activity)

        viewModel = ViewModelProviders.of(this)
            .get(MiniAppDwnlViewModel::class.java).apply {
                miniAppView.observe(this@MiniAppDisplayActivity, Observer {
                    if (ApplicationInfo.FLAG_DEBUGGABLE == 2)
                        WebView.setWebContentsDebuggingEnabled(true)
                    //action: display webview
                    setContentView(it)
                })
            }

        val appId = "c028be14-4ded-4734-acc6-2d35d9a67630"
        val versionId = "451222a7-a8f7-41c2-8394-c790788ad9d4"
        launch {
            viewModel.obtainMiniAppView(appId, versionId, this@MiniAppDisplayActivity)
        }
    }
}
