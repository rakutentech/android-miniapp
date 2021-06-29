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

class PointsActivity : BaseActivity() {
    private lateinit var settings: AppSettings
    private lateinit var binding: PointsActivityBinding

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
        binding = DataBindingUtil.setContentView(this, R.layout.points_activity)
        settings.points?.standard?.let { binding.edtPointStandard.setText(it.toString()) }
        settings.points?.term?.let { binding.edtPointTimeLimited.setText(it.toString()) }
        settings.points?.cash?.let { binding.edtPointRakutenCash.setText(it.toString()) }
    }

    private fun update() {
        settings.points = Points(
                edtPointStandard.text.toString().toInt(),
                edtPointTimeLimited.text.toString().toInt(),
                edtPointRakutenCash.text.toString().toInt()
        )
        hideSoftKeyboard(binding.root)
        finish()
    }
}
