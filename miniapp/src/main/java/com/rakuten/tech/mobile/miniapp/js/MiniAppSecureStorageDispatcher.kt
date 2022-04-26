package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage
import java.io.*

internal class MiniAppSecureStorageDispatcher {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var activity: Activity
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var miniAppId: String
    private lateinit var secureStorage: MiniAppSecureStorage

    fun setBridgeExecutor(activity: Activity, bridgeExecutor: MiniAppBridgeExecutor) {
        this.activity = activity
        this.bridgeExecutor = bridgeExecutor
    }

    fun setMiniAppComponents(miniAppId: String, customPermissionCache: MiniAppCustomPermissionCache) {
        this.miniAppId = miniAppId
        this.customPermissionCache = customPermissionCache
        this.secureStorage = MiniAppSecureStorage(activity)
        onLoad()
    }

    @Suppress("ComplexCondition")
    private fun <T> whenReady(callback: () -> T) {
        if (this::bridgeExecutor.isInitialized &&
            this::activity.isInitialized &&
            this::miniAppId.isInitialized &&
            this::customPermissionCache.isInitialized &&
            this::secureStorage.isInitialized
        ) {
            callback.invoke()
        }
    }

    private fun onLoad(){
       val data = secureStorage.readFromEncryptedFile(miniAppId)
    }

    fun onSetItems(callbackId: String, jsonStr: String) = whenReady {
        //TODO: Parse items from jsonStr
        val items: Map<String, String> = mapOf("key1" to "x", "key2" to "y", "key3" to "zz")
        secureStorage.insertSecureStorageItem(miniAppId, items)
    }

    fun onGetItem(callbackId: String, jsonStr: String) = whenReady {
        val deSerializedItems = secureStorage.readFromEncryptedFile(miniAppId)
        deSerializedItems?.let {
            Log.e("items", it.toString())
        }
    }

    fun onRemoveItems(callbackId: String, jsonStr: String) = whenReady {
        secureStorage.deleteSecureStorageItem(miniAppId, setOf("key1", "key2"))
    }

    fun onClearAll(callbackId: String) = whenReady {
        secureStorage.deleteSecureStorage(miniAppId)
    }

    fun onSize(callbackId: String) = whenReady {
        val size = secureStorage.secureStorageSize(miniAppId)
        Log.e("size", size)
    }

    fun onReady() = whenReady {

    }
}
