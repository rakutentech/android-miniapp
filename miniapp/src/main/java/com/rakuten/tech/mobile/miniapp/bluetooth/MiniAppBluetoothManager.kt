package com.rakuten.tech.mobile.miniapp.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

class MiniAppBluetoothManager {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var activity: Activity
    private var hasBluetoothFeature = false

    fun initialize(activity: Activity) {
        this.activity = activity
        this.bluetoothManager = activity.getSystemService(BluetoothManager::class.java)
        this.bluetoothAdapter = bluetoothManager.adapter
        this.hasBluetoothFeature =
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (hasBluetoothFeature && bluetoothAdapter.isEnabled) {
            checkPermissions()
            bluetoothAdapter.startDiscovery()
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    1
                )
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    1
                )
            }
        }
    }

    fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        activity.registerReceiver(receiver, filter)
    }

    fun unregisterReceiver(receiver: BroadcastReceiver) {
        activity.unregisterReceiver(receiver)
    }
}
