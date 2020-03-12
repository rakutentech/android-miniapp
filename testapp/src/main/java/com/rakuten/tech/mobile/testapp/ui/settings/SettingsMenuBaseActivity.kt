package com.rakuten.tech.mobile.testapp.ui.settings

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity

abstract class SettingsMenuBaseActivity: BaseActivity() {

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
        settingsDialog: View?,
        appId: EditText,
        subscriptionKey: EditText
    ) {
        AlertDialog.Builder(this)
            .setTitle(R.string.lb_app_settings)
            .setView(settingsDialog)
            .setPositiveButton(R.string.action_save) { dialog, _ ->
                dialog.dismiss()
                updateSettings(appId.text.toString(), subscriptionKey.text.toString())
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun updateSettings(appId: String, subscriptionKey: String) {
        settings.appId = appId
        settings.subscriptionKey = subscriptionKey

        recreate()
    }
}
