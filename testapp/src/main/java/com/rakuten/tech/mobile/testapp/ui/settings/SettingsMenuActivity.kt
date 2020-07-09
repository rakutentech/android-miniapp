package com.rakuten.tech.mobile.testapp.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.isInvalidUuid
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import kotlinx.coroutines.launch

class SettingsMenuActivity : BaseActivity() {

    private lateinit var settings: AppSettings

    private lateinit var menuSave: MenuItem
    private lateinit var textInfo: TextView
    private lateinit var edtAppId: EditText
    private lateinit var edtSubscriptionKey: EditText
    private lateinit var switchTestMode: SwitchMaterial
    private lateinit var settingsProgressDialog: SettingsProgressDialog

    private val settingTextWatcher = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            enableSaveView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings.instance
        setContentView(R.layout.settings_menu_activity)

        initializeActionBar()
        initializeUIComponents()

        renderAppSettingsScreen()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        menuSave = menu.findItem(R.id.settings_menu_save)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.settings_menu_save -> {
                onSaveAction()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onSaveAction() {
        settingsProgressDialog.show()

        updateSettings(
            edtAppId.text.toString(),
            edtSubscriptionKey.text.toString(),
            switchTestMode.isChecked
        )
    }

    private fun initializeActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun initializeUIComponents() {
        textInfo = findViewById(R.id.textInfo)
        edtAppId = findViewById(R.id.editAppId)
        edtSubscriptionKey = findViewById(R.id.editSubscriptionKey)
        switchTestMode = findViewById(R.id.switchTestMode)
        settingsProgressDialog =
            SettingsProgressDialog(
                this
            )
    }

    private fun renderAppSettingsScreen() {
        textInfo.text = createSettingsInfo()
        edtAppId.setText(settings.appId)
        edtSubscriptionKey.setText(settings.subscriptionKey)
        switchTestMode.isChecked = settings.isTestMode

        edtAppId.addTextChangedListener(settingTextWatcher)
        edtSubscriptionKey.addTextChangedListener(settingTextWatcher)

        enableSaveView()
    }

    private fun createSettingsInfo(): String {
        return "Build " + getString(R.string.miniapp_sdk_version) + " - " +
                getString(R.string.build_version)
    }

    private fun enableSaveView() {
        if (::menuSave.isInitialized && ::edtAppId.isInitialized && ::edtSubscriptionKey.isInitialized) {
            menuSave.isEnabled =
                !(edtAppId.text.toString().isInvalidUuid() || edtSubscriptionKey.text.isEmpty())
        }
    }

    private fun updateSettings(appId: String, subscriptionKey: String, isTestMode: Boolean) {
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
                    settingsProgressDialog.cancel()
                    finish()
                }
            } catch (error: MiniAppSdkException) {
                settings.appId = appIdHolder
                settings.subscriptionKey = subscriptionKeyHolder
                settings.isTestMode = isTestModeHolder
                runOnUiThread {
                    settingsProgressDialog.cancel()
                    val toast = Toast.makeText(this@SettingsMenuActivity, error.message, Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.TOP, 0, 0)
                    toast.show()
                }
            }
        }
    }
}
