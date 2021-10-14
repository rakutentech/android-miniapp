package com.rakuten.tech.mobile.testapp.ui.display

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppExternalUrlLoader
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.component.SampleExternalWebView
import com.rakuten.tech.mobile.testapp.ui.component.SampleWebViewClient

class WebViewActivity : BaseActivity() {
    private lateinit var sampleExternalWebView: SampleExternalWebView

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    companion object {
        val loadUrlTag = "load_url_tag"
        val miniAppIdTag = "miniapp_id_tag"
        val appUrlTag = "miniapp_url_tag"

        fun startForResult(activity: Activity, url: String, appId: String?, appUrl: String? = null, externalWebViewReqCode: Int) {
            val intent = Intent(activity, WebViewActivity::class.java).apply {
                putExtra(loadUrlTag, url)
                appId?.let { putExtra(miniAppIdTag, it) }
                appUrl?.let { putExtra(appUrlTag, it) }
            }
            activity.startActivityForResult(intent, externalWebViewReqCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        title = "External WebView Sample"

        val appUrl = intent.getStringExtra(appUrlTag) ?: ""
        val miniAppExternalUrlLoader = if (appUrl.isNotBlank()) {
            MiniAppExternalUrlLoader.loaderWithUrl(appUrl, this)
        } else {
            MiniAppExternalUrlLoader.loaderWithId(intent.getStringExtra(miniAppIdTag) ?: "", this)
        }

        val webViewClient = SampleWebViewClient(miniAppExternalUrlLoader)

        sampleExternalWebView = SampleExternalWebView(this, intent.getStringExtra(loadUrlTag) ?: "", webViewClient)
        setContentView(sampleExternalWebView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            val returnIntent = Intent().apply { putExtra("isClosedByBackPressed", true) }
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (sampleExternalWebView.canGoBack())
            sampleExternalWebView.goBack()
        else {
            val returnIntent = Intent().apply { putExtra("isClosedByBackPressed", true) }
            setResult(Activity.RESULT_OK, returnIntent)
            super.onBackPressed()
        }
    }
}
