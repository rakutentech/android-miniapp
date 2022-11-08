package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.js.userinfo.Points
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.PointsActivityBinding
import com.rakuten.tech.mobile.testapp.helper.hideSoftKeyboard
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.points_activity.*
import java.lang.NumberFormatException

class PointsActivity : BaseActivity() {
    private lateinit var settings: AppSettings
    private lateinit var binding: PointsActivityBinding
    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, PointsActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings.instance
        showBackIcon()
        renderScreen()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            android.R.id.home -> {
                onExit()
                return true
            }
            R.id.settings_menu_save -> {
                updatePreferences()
                onExit()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renderScreen() {
        binding = DataBindingUtil.setContentView(this, R.layout.points_activity)
        settings.points.standard.let { binding.edtPointStandard.setText(it.toString()) }
        settings.points.term.let { binding.edtPointTimeLimited.setText(it.toString()) }
        settings.points.cash.let { binding.edtPointRakutenCash.setText(it.toString()) }
        updatePreferences()
    }

    private fun updatePreferences() {
        var pointStandard = edtPointStandard.text.toString()
        if (pointStandard == "") pointStandard = "0"
        var pointTimeLimited = edtPointTimeLimited.text.toString()
        if (pointTimeLimited == "") pointTimeLimited = "0"
        var pointRakutenCash = edtPointRakutenCash.text.toString()
        if (pointRakutenCash == "") pointRakutenCash = "0"
        try {
            settings.points = Points(
                    pointStandard.toInt(),
                    pointTimeLimited.toInt(),
                    pointRakutenCash.toInt()
            )
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

    private fun onExit() {
        hideSoftKeyboard(binding.root)
        finish()
    }
}
