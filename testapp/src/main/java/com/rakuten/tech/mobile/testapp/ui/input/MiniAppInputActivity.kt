package com.rakuten.tech.mobile.testapp.ui.input

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.webkit.URLUtil
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppInputActivityBinding
import com.rakuten.tech.mobile.testapp.AppScreen.MINI_APP_INPUT_ACTIVITY
import com.rakuten.tech.mobile.testapp.helper.isInvalidUuid
import com.rakuten.tech.mobile.testapp.launchActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.display.firsttime.PreloadMiniAppWindow
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import com.rakuten.tech.mobile.testapp.ui.settings.MenuBaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuActivity

class MiniAppInputActivity : MenuBaseActivity(), PreloadMiniAppWindow.PreloadMiniAppLaunchListener {

    private lateinit var binding: MiniAppInputActivityBinding
    private val preloadMiniAppWindow by lazy { PreloadMiniAppWindow(this, this) }

    sealed class InputDisplay(val input: String) {
        class AppId(input: String): InputDisplay(input)
        class Url(input: String): InputDisplay(input)
        class None: InputDisplay("")
    }
    private var display: InputDisplay = InputDisplay.None()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.mini_app_input_activity)

        setupInputHint()
        validateInput(binding.edtAppId.text.toString().trim())
        binding.edtAppId.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInput(s.toString().trim())
            }
        })

        binding.btnDisplayAppId.setOnClickListener {
            raceExecutor.run { displayMiniApp() }
        }
        binding.btnDisplayList.setOnClickListener {
            raceExecutor.run { launchActivity<MiniAppListActivity>() }
        }
    }

    private fun setupInputHint() {
        if (AppSettings.instance.isPreviewMode)
            binding.inputLayout.hint = getString(R.string.lb_app_url)
        else
            binding.inputLayout.hint = getString(R.string.lb_app_id_or_url)
    }

    private fun validateInput(input: String) {
        if (input.isBlank())
            binding.btnDisplayAppId.isEnabled = false
        else {
            display = if (URLUtil.isValidUrl(input)) {
                onValidUI()
                InputDisplay.Url(input)
            } else if (!AppSettings.instance.isPreviewMode && !input.isInvalidUuid()) {
                onValidUI()
                InputDisplay.AppId(input)
            } else {
                onInvalidUI()
                InputDisplay.None()
            }
        }
    }

    private fun onInvalidUI() {
        binding.edtAppId.error = getString(R.string.error_invalid_input)
        binding.btnDisplayAppId.isEnabled = false
    }

    private fun onValidUI() {
        binding.edtAppId.error = null
        binding.btnDisplayAppId.isEnabled = true
    }

    private fun displayMiniApp() = when(display) {
        is InputDisplay.AppId -> preloadMiniAppWindow.initiate(null, display.input.trim())
        is InputDisplay.Url -> MiniAppDisplayActivity.startUrl(this, display.input.trim())
        is InputDisplay.None -> {}
    }

    override fun navigateToScreen(): Boolean {
        val intent = Intent(this, SettingsMenuActivity::class.java)
        intent.putExtra(MENU_SCREEN_NAME, MINI_APP_INPUT_ACTIVITY)
        startActivity(intent)
        return true
    }

    override fun onPreloadMiniAppResponse(isAccepted: Boolean) {
        if (isAccepted)
            MiniAppDisplayActivity.start(this, display.input)
    }
}
