package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.QaSettingsActivityBinding
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class QASettingsActivity : BaseActivity() {

    private lateinit var settings: AppSettings
    private lateinit var binding: QaSettingsActivityBinding
    private var accessTokenErrorCacheData: MiniAppAccessTokenError? = null

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, QASettingsActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings.instance
        accessTokenErrorCacheData = settings.accessTokenError
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.qa_settings_activity)
        binding.activity = this
        renderScreen()
        startListeners()
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
        if(accessTokenErrorCacheData != null){
            // set up initial state.
            when {
                accessTokenErrorCacheData?.type != null -> {
                    binding.switchAuthFailure.isChecked = true
                    binding.switchOtherError.isChecked = false
                    binding.edtCustomErrorMessage.setText(accessTokenErrorCacheData?.message ?: "")
                }
                else -> {
                    binding.switchAuthFailure.isChecked = false
                    binding.switchOtherError.isChecked = true
                    binding.edtCustomErrorMessage.setText(accessTokenErrorCacheData?.message ?: "")
                }
            }
        }else{
            // default state
            binding.switchAuthFailure.isChecked = false
            binding.switchOtherError.isChecked = false
            binding.edtCustomErrorMessage.text.clear()
        }
    }

    private fun startListeners(){
        binding.switchAuthFailure.setOnCheckedChangeListener(listener)
        binding.switchOtherError.setOnCheckedChangeListener(listener)
    }

    private val listener =
        CompoundButton.OnCheckedChangeListener { view, isChecked ->
            setSwitchStates(view,isChecked)
        }

    private fun setSwitchStates(view: CompoundButton, isChecked: Boolean) {
        when(view.id){
            R.id.switchAuthFailure-> {
                if(isChecked) binding.switchOtherError.isChecked = false
            }
            R.id.switchOtherError->{
                if(isChecked) binding.switchAuthFailure.isChecked = false
            }
        }
    }

    private fun update() {
        // If Authorization failure checked then authorizationFailureError type will send.
        // If Unknown failure checked then custom type will send.
        when {
            binding.switchAuthFailure.isChecked -> {
                settings.accessTokenError =
                    MiniAppAccessTokenError.authorizationFailureError(
                        binding.edtCustomErrorMessage.text.toString()
                    )
            }
            binding.switchOtherError.isChecked -> {
                settings.accessTokenError =
                    MiniAppAccessTokenError.custom(
                        binding.edtCustomErrorMessage.text.toString()
                    )
            }
            else -> {
                settings.accessTokenError = null
            }
        }
        finish()
    }
}
