package com.rakuten.tech.mobile.testapp.ui.input

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppInputActivityBinding
import com.rakuten.tech.mobile.testapp.AppScreen.MINI_APP_INPUT_ACTIVITY
import com.rakuten.tech.mobile.testapp.helper.isInvalidUuid
import com.rakuten.tech.mobile.testapp.launchActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import com.rakuten.tech.mobile.testapp.ui.settings.MenuBaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuActivity

class MiniAppInputActivity : MenuBaseActivity() {

    private lateinit var binding: MiniAppInputActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.mini_app_input_activity)

        binding.edtAppId.requestFocus()
        validateAppId(binding.edtAppId.text.toString())
        binding.edtAppId.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateAppId(s.toString())
            }
        })

        binding.btnDisplay.setOnClickListener {
            raceExecutor.run { display() }
        }
        binding.btnDisplayList.setOnClickListener {
            raceExecutor.run { launchActivity<MiniAppListActivity>() }
        }
    }

    private fun validateAppId(appId: String) {
        if (appId.isBlank())
            binding.btnDisplay.isEnabled = false
        else {
            if (appId.isInvalidUuid()) {
                binding.edtAppId.error = getString(R.string.error_invalid_input)
                binding.btnDisplay.isEnabled = false
            } else {
                binding.edtAppId.error = null
                binding.btnDisplay.isEnabled = true
            }
        }
    }

    private fun display() {
        MiniAppDisplayActivity.start(
            this,
            binding.edtAppId.text.toString().trim()
        )
    }

    override fun navigateToScreen(): Boolean {
        val intent = Intent(this, SettingsMenuActivity::class.java)
        intent.putExtra(MENU_SCREEN_NAME, MINI_APP_INPUT_ACTIVITY)
        startActivity(intent)
        return true
    }
}
