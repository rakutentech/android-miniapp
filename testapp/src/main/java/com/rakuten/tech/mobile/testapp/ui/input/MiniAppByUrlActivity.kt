package com.rakuten.tech.mobile.testapp.ui.input

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppInputActivityBinding
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.display.preload.PreloadMiniAppWindow

class MiniAppByUrlActivity : BaseActivity(), PreloadMiniAppWindow.PreloadMiniAppLaunchListener {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    private lateinit var binding: MiniAppInputActivityBinding

    sealed class InputDisplay(val input: String) {
        class Url(input: String) : InputDisplay(input)
        class None : InputDisplay("")
    }

    private var display: InputDisplay = InputDisplay.None()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.mini_app_input_activity)

        binding.inputLayout.hint = getString(R.string.lb_app_url)

        validateInput(binding.edtAppId.text.toString().trim())
        binding.edtAppId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //intended
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //intended
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInput(s.toString().trim())
            }
        })

        binding.btnDisplayAppId.setOnClickListener {
            raceExecutor.run { displayMiniApp() }
        }
    }

    override fun onBackPressed() {
        exitPage()
    }

    private fun isValidAppUrl(input: String): Boolean = URLUtil.isValidUrl(input)
            && Patterns.WEB_URL.matcher(input).matches()

    private fun validateInput(input: String) {
        if (input.isBlank())
            binding.btnDisplayAppId.isEnabled = false
        else {
            display = if (isValidAppUrl(input)) {
                onValidUI()
                InputDisplay.Url(input)
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

    private fun displayMiniApp() = when (display) {
        is InputDisplay.Url -> {
            MiniAppDisplayActivity.startUrl(
                this,
                display.input.trim(),
            )
        }
        is InputDisplay.None -> {
            //intended
        }
    }

    override fun onPreloadMiniAppResponse(isAccepted: Boolean) {
        if (isAccepted)
            MiniAppDisplayActivity.start(this, display.input)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exitPage() {
        finish()
    }
}
