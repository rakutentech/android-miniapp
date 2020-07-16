package com.rakuten.tech.mobile.testapp.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.AppScreen.MINI_APP_INPUT_ACTIVITY
import com.rakuten.tech.mobile.testapp.AppScreen.MINI_APP_LIST_ACTIVITY
import com.rakuten.tech.mobile.testapp.helper.isInvalidUuid
import com.rakuten.tech.mobile.testapp.launchActivity
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.input.MiniAppInputActivity
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import com.rakuten.tech.mobile.testapp.ui.settings.MenuBaseActivity.Companion.MENU_SCREEN_NAME
import kotlinx.android.synthetic.main.settings_menu_activity.*
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SettingsMenuActivity : BaseActivity() {

    private lateinit var settings: AppSettings
    private lateinit var settingsProgressDialog: SettingsProgressDialog

    private var saveViewEnabled by Delegates.observable(true) { _, old, new ->
        if (new != old) {
            invalidateOptionsMenu()
        }
    }

    private val settingsTextWatcher = object : TextWatcher {
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
        settingsProgressDialog = SettingsProgressDialog(this)
        renderAppSettingsScreen()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        menu.findItem(R.id.settings_menu_save).isEnabled = saveViewEnabled
        return true
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
            editAppId.text.toString(),
            editSubscriptionKey.text.toString(),
            switchTestMode.isChecked
        )
    }

    private fun initializeActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        showBackIcon()
    }

    private fun renderAppSettingsScreen() {
        textInfo.text = createSettingsInfo()
        editAppId.setText(settings.appId)
        editSubscriptionKey.setText(settings.subscriptionKey)
        switchTestMode.isChecked = settings.isTestMode

        editAppId.addTextChangedListener(settingsTextWatcher)
        editSubscriptionKey.addTextChangedListener(settingsTextWatcher)

        enableSaveView()
    }

    private fun createSettingsInfo(): String {
        return "Build " + getString(R.string.miniapp_sdk_version) + " - " +
                getString(R.string.build_version)
    }

    private fun enableSaveView() {
        saveViewEnabled =
            !(editAppId.text.toString().isInvalidUuid() || editSubscriptionKey.text.isNullOrEmpty())
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
                    navigateToPreviousScreen()
                }
            } catch (error: MiniAppSdkException) {
                settings.appId = appIdHolder
                settings.subscriptionKey = subscriptionKeyHolder
                settings.isTestMode = isTestModeHolder
                runOnUiThread {
                    settingsProgressDialog.cancel()
                    val toast =
                        Toast.makeText(this@SettingsMenuActivity, error.message, Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.TOP, 0, 0)
                    toast.show()
                }
            }
        }
    }

    private fun navigateToPreviousScreen() {
        when (intent.extras?.getString(MENU_SCREEN_NAME)) {
            MINI_APP_LIST_ACTIVITY -> {
                raceExecutor.run { launchActivity<MiniAppListActivity>() }
            }
            MINI_APP_INPUT_ACTIVITY -> {
                raceExecutor.run { launchActivity<MiniAppInputActivity>() }
            }
            else -> finish()
        }
    }
}
