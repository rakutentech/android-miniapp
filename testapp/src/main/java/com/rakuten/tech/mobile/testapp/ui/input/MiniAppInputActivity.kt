package com.rakuten.tech.mobile.testapp.ui.input

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import kotlinx.android.synthetic.main.mini_app_input_activity.*

class MiniAppInputActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mini_app_input_activity)

        btnDisplay.setOnClickListener(onDisplayClick)
    }

    private val onDisplayClick = View.OnClickListener {
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

