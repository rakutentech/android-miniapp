package com.rakuten.tech.mobile.testapp.ui.userdata

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.bluetooth.BluetoothReceiverListenerDefault
import com.rakuten.tech.mobile.miniapp.bluetooth.MiniAppBluetoothManager
import com.rakuten.tech.mobile.miniapp.bluetooth.MiniAppBluetoothReceiverDefault
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.QaSettingsActivityBinding
import com.rakuten.tech.mobile.testapp.helper.hideSoftKeyboard
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class QASettingsActivity : BaseActivity(), BluetoothReceiverListenerDefault {
    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private lateinit var settings: AppSettings
    private lateinit var binding: QaSettingsActivityBinding
    private var accessTokenErrorCacheData: MiniAppAccessTokenError? = null
    private val miniApp = MiniApp.instance(AppSettings.instance.miniAppSettings)
    private val receiver = MiniAppBluetoothReceiverDefault(this)
    private val bluetoothManager = MiniAppBluetoothManager()
    private lateinit var menuBluetooth: MenuItem

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

        // detect bluetooth devices on Android 12+
        bluetoothManager.initialize(this)
        bluetoothManager.registerReceiver(receiver, receiver.bluetoothFilter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_qa_menu, menu)
        menuBluetooth = menu.findItem(R.id.qa_menu_bluetooth)
        menuBluetooth.isVisible = false
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
            R.id.qa_menu_bluetooth -> {
                requestBTConnectPermission()
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
    }

    private fun startListeners(){
        binding.switchAuthFailure.setOnCheckedChangeListener(accessTokenListener)
        binding.switchOtherError.setOnCheckedChangeListener(accessTokenListener)
        binding.switchUniqueIdError.setOnCheckedChangeListener { _, isChecked ->
            binding.edtUniqueIdError.isEnabled = isChecked
        }
        binding.btnClearAllSecureStorage.setOnClickListener {
            clearAllSecureStorage()
        }
        binding.btnDetectBTDevices.setOnClickListener {
            bluetoothManager.startDiscovery()
        }
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

        // post tasks
        hideSoftKeyboard(binding.root)
        finish()
    }

    private fun requestBTConnectPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1
            )
        }
    }

    override fun onDeviceFound(device: BluetoothDevice?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1
            )
        }
        menuBluetooth.isVisible = device?.name.toString().isNotEmpty()
    }

    override fun onDeviceDiscoveryStarted() {
        Snackbar.make(binding.root, "Detecting bluetooth devices nearby...", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDeviceDiscoveryFinished() {
        Snackbar.make(binding.root, "Finished detecting bluetooth devices.", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.unregisterReceiver(receiver)
    }
}
