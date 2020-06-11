package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.mini_app_display_activity.*

class MiniAppDisplayActivity : BaseActivity() {

    private lateinit var appId: String

    companion object {
        private val appIdTag = "app_id_tag"
        private val miniAppTag = "mini_app_tag"

        fun start(context: Context, appId: String) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appIdTag, appId)
            })
        }

        fun start(context: Context, miniAppInfo: MiniAppInfo) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(miniAppTag, miniAppInfo)
            })
        }
    }

    private lateinit var viewModel: MiniAppDisplayViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(miniAppTag) || intent.hasExtra(appIdTag)) {
            appId = intent.getStringExtra(appIdTag) ?: ""

            setContentView(R.layout.mini_app_display_activity)

            viewModel = ViewModelProvider.NewInstanceFactory()
                .create(MiniAppDisplayViewModel::class.java).apply {

                    setHostLifeCycle(lifecycle)
                    miniAppDisplay.observe(this@MiniAppDisplayActivity, Observer {
                        if (ApplicationInfo.FLAG_DEBUGGABLE == 2)
                            WebView.setWebContentsDebuggingEnabled(true)
                        //action: display webview
                        setContentView(it)
                    })

                    errorData.observe(this@MiniAppDisplayActivity, Observer {
                        Toast.makeText(this@MiniAppDisplayActivity, it, Toast.LENGTH_LONG).show()
                    })

                    isLoading.observe(this@MiniAppDisplayActivity, Observer {
                        toggleProgressLoading(it)
                    })
                }

            val miniAppMessageBridge = object: MiniAppMessageBridge() {
                override fun getUniqueId() = AppSettings.instance.uniqueId
            }

            if (appId.isEmpty())
                viewModel.obtainMiniAppDisplay(
                    this@MiniAppDisplayActivity,
                    intent.getParcelableExtra<MiniAppInfo>(miniAppTag)!!.id,
                    miniAppMessageBridge)
            else
                viewModel.obtainMiniAppDisplay(
                    this@MiniAppDisplayActivity,
                    appId,
                    miniAppMessageBridge)
        }
    }

    private fun toggleProgressLoading(isOn: Boolean) {
        if (findViewById<View>(R.id.pb) != null) {
            when (isOn) {
                true -> pb.visibility = View.VISIBLE
                false -> pb.visibility = View.GONE
            }
        }
    }
}
