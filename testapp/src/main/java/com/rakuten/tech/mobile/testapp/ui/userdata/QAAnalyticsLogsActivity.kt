package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.QaAnalyticsLogsActivityBinding
import com.rakuten.tech.mobile.testapp.helper.FileUtils
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity

class QAAnalyticsLogsActivity : BaseActivity() {
    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private lateinit var binding: QaAnalyticsLogsActivityBinding

    companion object {

        fun start(activity: Activity) {
            activity.startActivity(
                Intent(activity, QAAnalyticsLogsActivity::class.java),
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.qa_analytics_logs_activity)
        binding.activity = this
    }

    override fun onResume() {
        super.onResume()
        renderScreen()
    }

    private fun renderScreen() {
        binding.tvAnalyticsLogs.text = FileUtils.getAnalyticLogs(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            android.R.id.home -> {
                exitPage()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        exitPage()
    }

    private fun exitPage() {
        finish()
    }

}

