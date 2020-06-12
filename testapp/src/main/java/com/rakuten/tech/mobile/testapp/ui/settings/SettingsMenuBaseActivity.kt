package com.rakuten.tech.mobile.testapp.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.isInvalidUuid
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import kotlinx.coroutines.launch

abstract class SettingsMenuBaseActivity : BaseActivity() {

    private lateinit var settings: AppSettings
    
    private lateinit var btnSave: View
    private lateinit var edtAppId: EditText
    private lateinit var edtSubscriptionKey: EditText
    private val settingTextWatcher = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            validateSetting()
        }
    }

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

    protected fun showAppSettings(): Boolean {
        val settingsDialog = LayoutInflater.from(this)
            .inflate(R.layout.app_settings_dialog, null, false)

        edtAppId = settingsDialog.findViewById(R.id.app_id) as EditText
        edtSubscriptionKey = settingsDialog.findViewById(R.id.subscription_key) as EditText
        edtAppId.setText(settings.appId)
        edtSubscriptionKey.setText(settings.subscriptionKey)
        edtAppId.addTextChangedListener(settingTextWatcher)
        edtSubscriptionKey.addTextChangedListener(settingTextWatcher)
        validateSetting()

        renderAppSettingsDialog(settingsDialog, edtAppId, edtSubscriptionKey)
        return true
    }

    private fun renderAppSettingsDialog(
        settingsDialog: View,
        appId: EditText,
        subscriptionKey: EditText
    ) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("${resources.getString(R.string.lb_app_settings)} - Build " +
                    resources.getString(R.string.build_version))
            .setView(settingsDialog)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener { _dialog ->
            btnSave = (_dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                val pb = settingsDialog.findViewById<View>(R.id.pb)
                pb.visibility = View.VISIBLE
                updateSettings(appId.text.toString(), subscriptionKey.text.toString(), pb, _dialog)
            }

            _dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                _dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun validateSetting() {
        if (::btnSave.isInitialized && ::edtAppId.isInitialized && ::edtSubscriptionKey.isInitialized) {
            btnSave.isEnabled =
                !(edtAppId.text.toString().isInvalidUuid() || edtSubscriptionKey.text.isEmpty())
        }
    }

    private fun updateSettings(appId: String, subscriptionKey: String, pb: View, dialog: AlertDialog) {
        val appIdHolder = settings.appId
        val subscriptionKeyHolder = settings.subscriptionKey
        settings.appId = appId
        settings.subscriptionKey = subscriptionKey

        launch {
            try {
                MiniApp.instance(AppSettings.instance.miniAppSettings).listMiniApp()
                runOnUiThread {
                    pb.visibility = View.GONE
                    dialog.dismiss()
                    recreate()
                }
            } catch (error: MiniAppSdkException) {
                settings.appId = appIdHolder
                settings.subscriptionKey = subscriptionKeyHolder
                runOnUiThread {
                    pb.visibility = View.GONE
                    val toast = Toast.makeText(this@SettingsMenuBaseActivity, error.message, Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.TOP, 0, 0)
                    toast.show()
                }
            }
        }
    }
}
