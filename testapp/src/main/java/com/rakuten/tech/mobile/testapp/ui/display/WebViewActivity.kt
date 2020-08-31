package com.rakuten.tech.mobile.testapp.ui.display

import android.os.Bundle
import android.view.MenuItem
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppExternalUrlLoader
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.component.SampleWebView
import com.rakuten.tech.mobile.testapp.ui.component.SampleWebViewClient

class WebViewActivity: BaseActivity() {
    private lateinit var sampleWebView: SampleWebView

    companion object {
        val loadUrlTag = "load_url_tag"
        val miniAppIdTag = "miniapp_id_tag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        title = "External WebView Sample"

        val miniAppExternalUrlLoader = MiniAppExternalUrlLoader(
            intent.getStringExtra(miniAppIdTag) ?: "", this)
        val webViewClient = SampleWebViewClient(miniAppExternalUrlLoader)

        sampleWebView = SampleWebView(this, intent.getStringExtra(loadUrlTag) ?: "", webViewClient)
        setContentView(sampleWebView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (sampleWebView.canGoBack())
            sampleWebView.goBack()
        else
            super.onBackPressed()
    }
}
