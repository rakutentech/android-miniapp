package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.util.Log
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.errors.MiniAppStorageError
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage

internal class MiniAppSecureStorageDispatcher {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var activity: Activity
    private lateinit var miniAppId: String
    private lateinit var secureStorage: MiniAppSecureStorage
    private lateinit var storedItems: Map<String, String>

    fun setBridgeExecutor(activity: Activity, bridgeExecutor: MiniAppBridgeExecutor) {
        this.activity = activity
        this.bridgeExecutor = bridgeExecutor
    }

    fun setMiniAppComponents(miniAppId: String) {
        this.miniAppId = miniAppId
        this.secureStorage = MiniAppSecureStorage(activity)
        onLoad()
    }

    @Suppress("ComplexCondition")
    private fun <T> whenReady(callback: () -> T) {
        if (this::bridgeExecutor.isInitialized &&
            this::activity.isInitialized &&
            this::miniAppId.isInitialized &&
            this::secureStorage.isInitialized
        ) {
            callback.invoke()
        }
    }

    private fun onLoad(){
        val onSuccess = { items: Map<String, String> ->
            storedItems = items
            //TODO: Callback event
        }
        val onFailed = { error: MiniAppStorageError ->
            //TODO: Callback error event
        }
        secureStorage.loadSecureStorage(miniAppId, onSuccess, onFailed)
    }

    fun onSetItems(callbackId: String, jsonStr: String) = whenReady {
        //TODO: Parse items from jsonStr
        val items: Map<String, String> = mapOf("key1" to "x", "key2" to "y", "key3" to "zz")
        val onSuccess = {
            bridgeExecutor.postValue(callbackId, "successfully saved")
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(error))
        }
        secureStorage.insertSecureStorageItem(miniAppId, items, onSuccess, onFailed)
    }

    fun onGetItem(callbackId: String, jsonStr: String) = whenReady {
        if (!storedItems.isNullOrEmpty() && storedItems.containsKey("key1")) {
            bridgeExecutor.postValue(callbackId, storedItems["key1"] ?: "")
        } else {
            val onSuccess = { itemValue: String ->
                bridgeExecutor.postValue(callbackId, itemValue)
            }
            val onFailed = { error: MiniAppStorageError ->
                bridgeExecutor.postError(callbackId, Gson().toJson(error))
            }
            secureStorage.getItem(miniAppId, "key1", onSuccess, onFailed)
        }
    }

    fun onRemoveItems(callbackId: String, jsonStr: String) = whenReady {
        val onSuccess = {
            bridgeExecutor.postValue(callbackId, "successfully deleted")
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(error))
        }
        secureStorage.deleteSecureStorageItems(miniAppId, setOf("key1", "key2"), onSuccess, onFailed)
    }

    fun onClearAll(callbackId: String) = whenReady {
        val onSuccess = {
            bridgeExecutor.postValue(callbackId, "successfully deleted")
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(error))
        }
        secureStorage.deleteSecureStorage(miniAppId, onSuccess, onFailed)
    }

    fun onSize(callbackId: String) = whenReady {
        val onSuccess = { fileSize: String ->
            bridgeExecutor.postValue(callbackId, fileSize)
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(error))
        }
        secureStorage.secureStorageSize(miniAppId, onSuccess, onFailed)
    }

    fun onReady() = whenReady {
    }
}
