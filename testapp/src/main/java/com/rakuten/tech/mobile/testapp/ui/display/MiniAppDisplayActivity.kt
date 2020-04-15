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
import androidx.lifecycle.ViewModelProviders
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import kotlinx.android.synthetic.main.mini_app_display_activity.*
import kotlinx.coroutines.launch

class MiniAppDisplayActivity : BaseActivity() {

    private lateinit var appId: String

    companion object {
        private val appIdTag = "app_id_tag"

        fun start(context: Context, appId: String) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appIdTag, appId)
            })
        }
    }

    private lateinit var viewModel: MiniAppDisplayViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(appIdTag)) {
            appId = intent.getStringExtra(appIdTag) ?: ""

            setContentView(R.layout.mini_app_display_activity)

            viewModel = ViewModelProvider.NewInstanceFactory()
                .create(MiniAppDisplayViewModel::class.java).apply {

                    setHostLifeCycle(lifecycle)
                    miniAppView.observe(this@MiniAppDisplayActivity, Observer {
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

            launch {
                viewModel.obtainMiniAppView(appId)
            }
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
