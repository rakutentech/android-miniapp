package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.content.Intent
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

    private lateinit var appId: String
    private lateinit var versionId: String

    companion object {
        private val appIdTag = "app_id_tag"
        private val appVersionTag = "app_version"

        fun start(context: Context, appId: String, versionId: String) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appIdTag, appId)
                putExtra(appVersionTag, versionId)
            })
        }
    }

    private lateinit var viewModel: MiniAppDwnlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(appIdTag) && intent.hasExtra(appVersionTag)) {
            //default is Lookbook app for testing
            appId = intent.getStringExtra(appIdTag) ?: "c028be14-4ded-4734-acc6-2d35d9a67630"
            versionId = intent.getStringExtra(appVersionTag) ?: "451222a7-a8f7-41c2-8394-c790788ad9d4"

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

            launch {
                viewModel.obtainMiniAppView(appId, versionId, this@MiniAppDisplayActivity)
            }
        }
    }
}
