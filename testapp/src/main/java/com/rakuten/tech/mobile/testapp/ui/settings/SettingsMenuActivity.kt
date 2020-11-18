package com.rakuten.tech.mobile.testapp.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.SettingsMenuActivityBinding
import com.rakuten.tech.mobile.testapp.AppScreen.MINI_APP_INPUT_ACTIVITY
import com.rakuten.tech.mobile.testapp.AppScreen.MINI_APP_LIST_ACTIVITY
import com.rakuten.tech.mobile.testapp.helper.isInvalidUuid
import com.rakuten.tech.mobile.testapp.helper.isInputEmpty
import com.rakuten.tech.mobile.testapp.helper.showNormalDialog
import com.rakuten.tech.mobile.testapp.launchActivity
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.input.MiniAppInputActivity
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import com.rakuten.tech.mobile.testapp.ui.permission.MiniAppDownloadedListActivity
import com.rakuten.tech.mobile.testapp.ui.settings.MenuBaseActivity.Companion.MENU_SCREEN_NAME
import com.rakuten.tech.mobile.testapp.ui.userdata.AccessTokenActivity
import com.rakuten.tech.mobile.testapp.ui.userdata.ContactListActivity
import com.rakuten.tech.mobile.testapp.ui.userdata.ProfileSettingsActivity
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SettingsMenuActivity : BaseActivity() {

    private lateinit var settings: AppSettings
    private lateinit var settingsProgressDialog: SettingsProgressDialog
    private lateinit var binding: SettingsMenuActivityBinding

    private var saveViewEnabled by Delegates.observable(true) { _, old, new ->
        if (new != old) {
            invalidateOptionsMenu()
        }
    }

    private val settingsTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            validateInputIDs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings.instance
        binding = DataBindingUtil.setContentView(this, R.layout.settings_menu_activity)

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
            binding.editAppId.text.toString(),
            binding.editSubscriptionKey.text.toString(),
            binding.switchPreviewMode.isChecked
        )
    }

    private fun initializeActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        showBackIcon()
    }

    private fun renderAppSettingsScreen() {
        binding.textInfo.text = createBuildInfo()
        binding.editAppId.setText(settings.appId)
        binding.editSubscriptionKey.setText(settings.subscriptionKey)
        binding.switchPreviewMode.isChecked = settings.isPreviewMode

        binding.editAppId.addTextChangedListener(settingsTextWatcher)
        binding.editSubscriptionKey.addTextChangedListener(settingsTextWatcher)

        binding.buttonProfile.setOnClickListener {
            ProfileSettingsActivity.start(this@SettingsMenuActivity)
        }

        binding.buttonContacts.setOnClickListener {
            ContactListActivity.start(this@SettingsMenuActivity)
        }

        binding.buttonCustomPermissions.setOnClickListener {
            MiniAppDownloadedListActivity.start(this@SettingsMenuActivity)
        }

        binding.buttonAccessToken.setOnClickListener { AccessTokenActivity.start(this@SettingsMenuActivity) }

        validateInputIDs()
    }

    private fun createBuildInfo(): String {
        val sdkVersion = getString(R.string.miniapp_sdk_version)
        val buildVersion = getString(R.string.build_version)
        return "Build $sdkVersion - $buildVersion"
    }

    private fun validateInputIDs() {
        val isAppIdInvalid = binding.editAppId.text.toString().isInvalidUuid()

        saveViewEnabled = !(isInputEmpty(binding.editAppId)
                || isInputEmpty(binding.editSubscriptionKey)
                || isAppIdInvalid)

        if (isInputEmpty(binding.editAppId) || isAppIdInvalid) {
            binding.editAppId.error = getString(R.string.error_invalid_input)
        }

        if (isInputEmpty(binding.editSubscriptionKey)) {
            binding.editSubscriptionKey.error = getString(R.string.error_invalid_input)
        }
    }

    private fun updateSettings(appId: String, subscriptionKey: String, isPreviewMode: Boolean) {
        val appIdHolder = settings.appId
        val subscriptionKeyHolder = settings.subscriptionKey
        val isPreviewModeHolder = settings.isPreviewMode
        settings.appId = appId
        settings.subscriptionKey = subscriptionKey
        settings.isPreviewMode = isPreviewMode

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
                settings.isPreviewMode = isPreviewModeHolder
                runOnUiThread {
                    settingsProgressDialog.cancel()
                    showNormalDialog(this@SettingsMenuActivity, error.message.toString())
                }
            }
        }
    }

    private fun navigateToPreviousScreen() {
        when (intent.extras?.getString(MENU_SCREEN_NAME)) {
            MINI_APP_LIST_ACTIVITY -> {
                val intent = Intent(this, MiniAppListActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                setResult(Activity.RESULT_CANCELED, intent)
                startActivity(intent)
                finish()
            }
            MINI_APP_INPUT_ACTIVITY -> {
                raceExecutor.run { launchActivity<MiniAppInputActivity>() }
            }
            else -> finish()
        }
    }
}
