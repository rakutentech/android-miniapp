package com.rakuten.tech.mobile.testapp.ui.input

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.textfield.TextInputEditText
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import kotlinx.android.synthetic.main.mini_app_input_activity.*

class MiniAppInputActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MiniAppInputActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mini_app_input_activity)

        btnDisplay.setOnClickListener{ singleExecution.run { display() } }
        btnDisplayList.setOnClickListener { singleExecution.run { MiniAppListActivity.start(this) } }
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

