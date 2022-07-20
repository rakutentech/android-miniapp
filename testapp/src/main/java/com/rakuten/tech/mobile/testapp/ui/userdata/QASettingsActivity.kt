package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.QaSettingsActivityBinding
import com.rakuten.tech.mobile.testapp.helper.hideSoftKeyboard
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class QASettingsActivity : BaseActivity() {
    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private lateinit var settings: AppSettings
    private lateinit var binding: QaSettingsActivityBinding
    private var accessTokenErrorCacheData: MiniAppAccessTokenError? = null
    private var miniApp = MiniApp.instance(AppSettings.instance.miniAppSettings)

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
        startListeners()
    }

    override fun onResume() {
        super.onResume()
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
        // access token
        if (accessTokenErrorCacheData != null) {
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
        } else {
            // default state
            binding.switchAuthFailure.isChecked = false
            binding.switchOtherError.isChecked = false
            binding.edtCustomErrorMessage.text?.clear()
        }

        // unique id
        binding.edtUniqueIdError.isEnabled = settings.uniqueIdError.isNotEmpty()
        binding.edtUniqueIdError.setText(settings.uniqueIdError)
        binding.switchUniqueIdError.isChecked = settings.uniqueIdError.isNotEmpty()

        // messaging unique id
        binding.edtMessagingUniqueIdError.setText(settings.messagingUniqueIdError)

        // mauid
        binding.edtMauidError.setText(settings.mauIdError)

        val maxStorage = settings.maxStorageSizeLimitInMB
        binding.edtMaxStorageLimit.setText("Current limit is $maxStorage MB")

        invalidateMaxStorageField()
    }

    private fun startListeners(){
        binding.switchAuthFailure.setOnCheckedChangeListener(accessTokenListener)
        binding.switchOtherError.setOnCheckedChangeListener(accessTokenListener)
        binding.switchUniqueIdError.setOnCheckedChangeListener { _, isChecked ->
            binding.edtUniqueIdError.isEnabled = isChecked
        }
        binding.edtMaxStorageLimit.setOnFocusChangeListener { _, _ ->
            binding.edtMaxStorageLimit.setText("")
        }
        binding.btnClearMiniAppSecureStorage.setOnClickListener{
            clearSecureStorageForMiniApp(binding.clearStorageForMiniAppId.text.toString())
        }
        binding.btnClearAllSecureStorage.setOnClickListener {
            clearAllSecureStorage()
        }
    }

    private fun invalidateMaxStorageField() {

        binding.clearStorageForMiniAppId.isEnabled = false
        binding.clearStorageForMiniAppId.setText("No MiniApp ID available.")
        binding.btnClearMiniAppSecureStorage.isEnabled = false

        var miniAppId = ""
        this.databaseList().forEach {
            val dbNamePrefix = "rmapp-"
            if (it.startsWith(dbNamePrefix)) {
                miniAppId = it.substring(dbNamePrefix.length)
            }
        }
        if(miniAppId.isNotEmpty()) {
            binding.clearStorageForMiniAppId.setText(miniAppId)
            binding.btnClearMiniAppSecureStorage.isEnabled = true
        }
    }

    private fun clearSecureStorageForMiniApp(miniAppId: String) {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        if (miniApp.clearSecureStorage(miniAppId)) {
                            Toast.makeText(
                                this@QASettingsActivity,
                                "MiniApp Secured Storage Cleared Successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@QASettingsActivity,
                                "Could not find the MiniApp to clear the secured storage!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        invalidateMaxStorageField()
                    }
                }
                dialog.dismiss()
            }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@QASettingsActivity)
        builder.setMessage("Are you sure to clear secure storage for this MiniApp ?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }

    private fun clearAllSecureStorage() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        miniApp.clearSecureStorage()
                        Toast.makeText(
                            this@QASettingsActivity,
                            "Successfully cleared all secured storage!",
                            Toast.LENGTH_LONG
                        ).show()
                        invalidateMaxStorageField()
                    }
                }
                dialog.dismiss()
            }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this@QASettingsActivity)
        builder.setMessage("Are you sure to clear all secure storage?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }

    private val accessTokenListener =
        CompoundButton.OnCheckedChangeListener { view, isChecked ->
            setAccessTokenSwitchStates(view,isChecked)
        }

    private fun setAccessTokenSwitchStates(view: CompoundButton, isChecked: Boolean) {
        when (view.id) {
            R.id.switchAuthFailure -> {
                if (isChecked) binding.switchOtherError.isChecked = false
            }
            R.id.switchOtherError -> {
                if (isChecked) binding.switchAuthFailure.isChecked = false
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

        // Save unique ID error response
        if (binding.switchUniqueIdError.isChecked) {
            if (binding.edtUniqueIdError.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please input error message for Unique ID", Toast.LENGTH_LONG).show()
                return
            } else settings.uniqueIdError = binding.edtUniqueIdError.text.toString()
        } else settings.uniqueIdError = ""

        //Save contact ID error response
        if (binding.edtMessagingUniqueIdError.text.isNullOrEmpty()) {
            settings.messagingUniqueIdError = ""
        } else {
            settings.messagingUniqueIdError = binding.edtMessagingUniqueIdError.text.toString()
        }

        //Save mauID error response
        if (binding.edtMauidError.text.isNullOrEmpty()) {
            settings.mauIdError = ""
        } else {
            settings.mauIdError = binding.edtMauidError.text.toString()
        }

        val upgradedSize = binding.edtMaxStorageLimit.text
        if (!upgradedSize.isNullOrEmpty() && upgradedSize.isDigitsOnly()) {
            val maxStorageSizeLimitInMB = binding.edtMaxStorageLimit.text.toString().toInt()
            settings.maxStorageSizeLimitInMB = maxStorageSizeLimitInMB
        }

        // post tasks
        hideSoftKeyboard(binding.root)
        finish()
    }
}
