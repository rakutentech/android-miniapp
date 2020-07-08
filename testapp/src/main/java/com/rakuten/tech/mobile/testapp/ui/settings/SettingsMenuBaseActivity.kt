package com.rakuten.tech.mobile.testapp.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
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

    private fun showAppSettings(): Boolean {
        val settingsView = LayoutInflater.from(this)
            .inflate(R.layout.settings_menu_base_activity, null, false)

        edtAppId = settingsView.findViewById(R.id.app_id) as EditText
        edtSubscriptionKey = settingsView.findViewById(R.id.subscription_key) as EditText
        val switchTest = settingsView.findViewById<SwitchCompat>(R.id.switchTest)
        edtAppId.setText(settings.appId)
        edtSubscriptionKey.setText(settings.subscriptionKey)
        edtAppId.addTextChangedListener(settingTextWatcher)
        edtSubscriptionKey.addTextChangedListener(settingTextWatcher)
        switchTest.isChecked = settings.isTestMode
        validateSetting()

        renderAppSettingsScreen(settingsView, edtAppId, edtSubscriptionKey, switchTest)
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun renderAppSettingsScreen(
        settingsView: View,
        appId: EditText,
        subscriptionKey: EditText,
        switchTest: SwitchCompat
    ) {
        title = resources.getString(R.string.lb_app_settings)

        setContentView(settingsView)

        val infoView = settingsView.findViewById<TextView>(R.id.textInfo)
        infoView.text = "Build " + resources.getString(R.string.miniapp_sdk_version) + " - " +
                resources.getString(R.string.build_version)

        btnSave = settingsView.findViewById(R.id.buttonSave)
        btnSave.setOnClickListener {
            val pb = settingsView.findViewById<View>(R.id.pb)
            pb.visibility = View.VISIBLE

            updateSettings(
                appId.text.toString(),
                subscriptionKey.text.toString(),
                switchTest.isChecked,
                pb
            )
        }

        val btnCancel = settingsView.findViewById<Button>(R.id.buttonCancel)
        btnCancel.setOnClickListener {
            launch {
                runOnUiThread {
                    settingsView.visibility = View.GONE
                    recreate()
                }
            }
        }
    }

    private fun validateSetting() {
        if (::btnSave.isInitialized && ::edtAppId.isInitialized && ::edtSubscriptionKey.isInitialized) {
            btnSave.isEnabled =
                !(edtAppId.text.toString().isInvalidUuid() || edtSubscriptionKey.text.isEmpty())
        }
    }

    private fun updateSettings(appId: String, subscriptionKey: String, isTestMode: Boolean,
                               pb: View) {
        val appIdHolder = settings.appId
        val subscriptionKeyHolder = settings.subscriptionKey
        val isTestModeHolder = settings.isTestMode
        settings.appId = appId
        settings.subscriptionKey = subscriptionKey
        settings.isTestMode = isTestMode

        launch {
            try {
                MiniApp.instance(AppSettings.instance.miniAppSettings).listMiniApp()
                settings.isSettingSaved = true
                runOnUiThread {
                    pb.visibility = View.GONE
                    recreate()
                }
            } catch (error: MiniAppSdkException) {
                settings.appId = appIdHolder
                settings.subscriptionKey = subscriptionKeyHolder
                settings.isTestMode = isTestModeHolder
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
