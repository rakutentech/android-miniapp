package com.rakuten.tech.mobile.miniapp.bluetooth

import android.bluetooth.BluetoothDevice

interface BluetoothReceiverListenerDefault {
    fun onDeviceFound(device: BluetoothDevice?)
    fun onDeviceDiscoveryStarted()
    fun onDeviceDiscoveryFinished()
}
