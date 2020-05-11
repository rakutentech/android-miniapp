package com.rakuten.tech.mobile.testapp.ui.input

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.isInvalidUuid
import com.rakuten.tech.mobile.testapp.launchActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuBaseActivity
import kotlinx.android.synthetic.main.mini_app_input_activity.*

class MiniAppInputActivity : SettingsMenuBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mini_app_input_activity)

        edtAppId.requestFocus()
        validateAppId(edtAppId.text.toString())
        edtAppId.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateAppId(s.toString())
            }
        })

        btnDisplay.setOnClickListener {
            raceExecutor.run { display() }
        }
        btnDisplayList.setOnClickListener {
            raceExecutor.run { launchActivity<MiniAppListActivity>() }
        }
    }

    private fun validateAppId(appId: String) {
        if (appId.isBlank())
            btnDisplay.isEnabled = false
        else {
            if (appId.isInvalidUuid()) {
                edtAppId.error = getString(R.string.error_invalid_input)
                btnDisplay.isEnabled = false
            } else {
                edtAppId.error = null
                btnDisplay.isEnabled = true
            }
        }
    }

    private fun display() {
        MiniAppDisplayActivity.start(
            this,
            edtAppId.text.toString().trim()
        )
    }
}

