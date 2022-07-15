package com.rakuten.tech.mobile.miniapp.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MiniAppBluetoothManager {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var activity: Activity

    companion object {
        const val REQ_CODE_BT_CONNECT = 10010
    }

    fun initialize(activity: Activity) {
        this.activity = activity
        this.bluetoothAdapter = activity.getSystemService(BluetoothManager::class.java).adapter
    }

    fun hasBTConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQ_CODE_BT_CONNECT
                )
            } else return true
        }

        return false
    }

    @SuppressLint("MissingPermission")
    fun detectPairedDevice(): Boolean {
        // run every 5 sec
        return bluetoothAdapter.bondedDevices.size > 0
    }
}
