package com.rakuten.tech.mobile.testapp.ui.input

import android.os.Bundle
import com.google.android.material.textfield.TextInputEditText
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.launchActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuBaseActivity
import kotlinx.android.synthetic.main.mini_app_input_activity.*
import java.lang.Exception
import java.util.UUID

class MiniAppInputActivity : SettingsMenuBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mini_app_input_activity)

        btnDisplay.setOnClickListener {
            raceExecutor.run { display() }
        }
        btnDisplayList.setOnClickListener {
            raceExecutor.run { launchActivity<MiniAppListActivity>() }
        }
    }

    private fun display() {
        if (isValidInput(edtAppId)) {
            MiniAppDisplayActivity.start(
                this,
                edtAppId.text.toString().trim()
            )
        }
    }

    private fun isValidInput(edt: TextInputEditText): Boolean =
        if (isInvalidUuid(edt.text.toString())) {
            edt.error = getString(R.string.error_invalid_input)
            false
        } else {
            edt.error = null
            true
        }

    private fun isInvalidUuid(input: String): Boolean = try {
        UUID.fromString(input)
        false
    } catch (e: Exception) {
        true
    }
}

