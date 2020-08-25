package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.component.SampleWebView

class WebViewActivity: BaseActivity() {
    private lateinit var sampleWebView: SampleWebView

    companion object {
        private val urlTag = "url_tag"

        fun start(context: Context, url: String) {
            context.startActivity(Intent(context, WebViewActivity::class.java).apply {
                putExtra(urlTag, url)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        title = "External WebView Sample"

        sampleWebView = SampleWebView(this, intent.getStringExtra(urlTag) ?: "")
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
