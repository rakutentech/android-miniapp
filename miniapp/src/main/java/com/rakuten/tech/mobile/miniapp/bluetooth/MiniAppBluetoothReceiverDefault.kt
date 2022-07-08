package com.rakuten.tech.mobile.miniapp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class MiniAppBluetoothReceiverDefault(val listener: BluetoothReceiverListenerDefault) :
    BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                listener.onDeviceFound(device)
            }
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                listener.onDeviceDiscoveryStarted()
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                listener.onDeviceDiscoveryFinished()
            }
        }
    }

    val bluetoothFilter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }
}
