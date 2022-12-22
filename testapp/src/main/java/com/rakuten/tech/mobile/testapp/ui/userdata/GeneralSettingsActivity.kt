package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.GeneralSettingsActivityBinding
import com.rakuten.tech.mobile.testapp.helper.hideSoftKeyboard
import com.rakuten.tech.mobile.testapp.helper.showToastMessage
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.deeplink.DynamicDeepLinkActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import java.net.URISyntaxException
import java.net.URL

class GeneralSettingsActivity : BaseActivity() {
    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private lateinit var settings: AppSettings
    private lateinit var binding: GeneralSettingsActivityBinding

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, GeneralSettingsActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings.instance
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.general_settings_activity)
        binding.activity = this
        setViewsListener()
        renderScreen()
    }

    private fun setViewsListener() {
        binding.buttonDeeplink.setOnClickListener {
            DynamicDeepLinkActivity.start(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            android.R.id.home -> {
                exitPage()
                return true
            }
            R.id.settings_menu_save -> {
                update()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exitPage() {
        hideSoftKeyboard(binding.root)
        finish()
    }

    private fun renderScreen() {
        binding.buttonDeeplink.setIcon(getDrawable(R.drawable.ic_settings_deeplink))
        binding.editParametersUrl.setText(settings.urlParameters)
    }

    private fun update() {
        val lastSavedUrlParameters = settings.urlParameters
        try {
            settings.urlParameters = binding.editParametersUrl.text.toString()
            URL("https://www.test-param.com?${binding.editParametersUrl.text.toString()}").toURI()
            exitPage()
        } catch (e: URISyntaxException) {
            settings.urlParameters = lastSavedUrlParameters
            showToastMessage(e.message ?: "Error update data")
        }
    }

}
