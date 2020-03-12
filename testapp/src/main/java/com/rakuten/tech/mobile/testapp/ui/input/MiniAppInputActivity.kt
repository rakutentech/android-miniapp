package com.rakuten.tech.mobile.testapp.ui.input

import android.os.Bundle
import com.google.android.material.textfield.TextInputEditText
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.launchActivity
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuBaseActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import kotlinx.android.synthetic.main.mini_app_input_activity.*

class MiniAppInputActivity : SettingsMenuBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mini_app_input_activity)

        btnDisplay.setOnClickListener{ raceExecutor.run { display() } }
        btnDisplayList.setOnClickListener { raceExecutor.run { launchActivity<MiniAppListActivity>() } }
    }

    private fun display() {
        val isAppIdValid = isValidInput(edtAppId)
        val isVersionIdValid = isValidInput(edtVersionId)

        if (isAppIdValid && isVersionIdValid)
            MiniAppDisplayActivity.start(this,
                edtAppId.text.toString().trim(), edtVersionId.text.toString().trim())
    }

    private fun isValidInput(edt: TextInputEditText): Boolean =
        if (edt.text.toString().trim().isEmpty()) {
            edt.error = getString(R.string.error_empty)
            false
        } else {
            edt.error = null
            true
        }
}

