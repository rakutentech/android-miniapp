package com.rakuten.tech.mobile.testapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import kotlinx.coroutines.launch

abstract class SettingsMenuBaseActivity : BaseActivity() {

    private lateinit var settings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings.instance
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.set_app_id_and_key -> showAppSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAppSettings(): Boolean {
        val settingsDialog = LayoutInflater.from(this)
            .inflate(R.layout.app_settings_dialog, null, false)

        val appId = settingsDialog.findViewById(R.id.app_id) as EditText
        val subscriptionKey = settingsDialog.findViewById(R.id.subscription_key) as EditText
        appId.setText(settings.appId)
        subscriptionKey.setText(settings.subscriptionKey)

        renderAppSettingsDialog(settingsDialog, appId, subscriptionKey)
        return true
    }

    private fun renderAppSettingsDialog(
        settingsDialog: View,
        appId: EditText,
        subscriptionKey: EditText
    ) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.lb_app_settings)
            .setView(settingsDialog)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener { _dialog ->
            (_dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pb = settingsDialog.findViewById<View>(R.id.pb)
                pb.visibility = View.VISIBLE
                updateSettings(appId.text.toString(), subscriptionKey.text.toString(), pb)
            }

            _dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                _dialog.dismiss()
            }
        }

        dialog.show()
    }


    private fun updateSettings(appId: String, subscriptionKey: String, pb: View) {
        val appIdHolder = settings.appId
        val subscriptionKeyHolder = settings.subscriptionKey
        settings.appId = appId
        settings.subscriptionKey = subscriptionKey

        launch {
            try {
                MiniApp.instance(AppSettings.instance.miniAppSettings).listMiniApp()
                runOnUiThread {
                    pb.visibility = View.GONE
                    recreate()
                }
            } catch (error: MiniAppSdkException) {
                settings.appId = appIdHolder
                settings.subscriptionKey = subscriptionKeyHolder
                runOnUiThread {
                    pb.visibility = View.GONE
                    Toast.makeText(this@SettingsMenuBaseActivity, error.message, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}
