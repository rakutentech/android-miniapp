package com.rakuten.tech.mobile.testapp.ui.display

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.component.SampleWebView
import com.rakuten.tech.mobile.testapp.ui.component.SampleWebViewClient

class WebViewActivity: BaseActivity() {
    private lateinit var sampleWebView: SampleWebView

    companion object {
        val loadUrlTag = "load_url_tag"
        val miniAppUrlSchemesTag = "miniapp_url_schemes_tag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        title = "External WebView Sample"
        val finishCallBack: (url: String) -> Unit = {
            val returnIntent = Intent().apply { putExtra(loadUrlTag, it) }
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        val webViewClient = SampleWebViewClient(finishCallBack,
            intent.getStringArrayExtra(miniAppUrlSchemesTag) ?: emptyArray())
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
