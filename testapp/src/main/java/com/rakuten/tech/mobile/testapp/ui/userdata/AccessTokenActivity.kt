package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.AccessTokenSettingsActivityBinding
import com.rakuten.tech.mobile.testapp.helper.parseDateToString
import com.rakuten.tech.mobile.testapp.helper.parseStringToDate
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import java.util.*

class AccessTokenActivity : BaseActivity() {

    private lateinit var settings: AppSettings
    private lateinit var accessToken: String
    private var expiredDate: Long = 0
    private lateinit var binding: AccessTokenSettingsActivityBinding
    private val dateFormat = "yyyy-MM-dd"

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, AccessTokenActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings.instance
        val tokenData = settings.tokenData
        accessToken = tokenData.token
        expiredDate = tokenData.validUntil

        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.access_token_settings_activity)
        binding.activity = this
        renderScreen()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.settings_menu_save -> {
                update()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renderScreen() {
        binding.edtToken.setText(accessToken)
        binding.edtDateExpired.setText(parseDateToString(dateFormat, Date(expiredDate)))
    }

    fun setExpiredDate() {
        val cal = Calendar.getInstance()
        cal.time = Date(expiredDate)
        DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val expiredDateStr = "$year-${(monthOfYear + 1)}-$dayOfMonth"
                binding.edtDateExpired.setText(expiredDateStr)
                expiredDate = parseStringToDate(dateFormat, expiredDateStr).time
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun update() {
        settings.tokenData = TokenData(accessToken, expiredDate)
        finish()
    }

    fun setAccessToken(text: CharSequence) {
        accessToken = text.toString()
    }
}
