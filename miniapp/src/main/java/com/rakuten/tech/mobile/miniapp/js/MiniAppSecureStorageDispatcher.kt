package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.errors.MiniAppStorageError
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage
import com.rakuten.tech.mobile.miniapp.storage.StorageState

internal class MiniAppSecureStorageDispatcher(
    private val storageMaxSizeKB: Int
) {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var activity: Activity
    private lateinit var miniAppId: String
    private lateinit var secureStorage: MiniAppSecureStorage
    private var cachedItems: Map<String, String>? = null
    private var storageState: StorageState = StorageState.DEFAULT
    private val stateObserver = Observer<StorageState> { state ->
        storageState = state
    }

    fun setBridgeExecutor(activity: Activity, bridgeExecutor: MiniAppBridgeExecutor) {
        this.activity = activity
        this.bridgeExecutor = bridgeExecutor
    }

    fun setMiniAppComponents(miniAppId: String) {
        this.miniAppId = miniAppId
        this.secureStorage = MiniAppSecureStorage(activity)
        secureStorage.storageState.observeForever(stateObserver)
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

    private fun onLoad() = whenReady {
        val onSuccess = { items: Map<String, String> ->
            cachedItems = items
            bridgeExecutor.dispatchEvent(
                eventType = NativeEventType.MINIAPP_SECURE_STORAGE_READY.value,
                value = Gson().toJson(SecureStorageReadyCallback(true))
            )
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.dispatchEvent(
                eventType = NativeEventType.MINIAPP_SECURE_STORAGE_READY.value,
                value = Gson().toJson(SecureStorageReadyCallback(false, error))
            )
        }
        secureStorage.loadSecureStorage(miniAppId, onSuccess, onFailed)
    }

    fun onSetItems(callbackId: String, jsonStr: String) = whenReady {
        val callbackObj: SecureStorageCallbackObj? =
            Gson().fromJson(jsonStr, SecureStorageCallbackObj::class.java)
        if (callbackObj != null) {
            val onSuccess = { items: Map<String, String> ->
                cachedItems = items
                bridgeExecutor.postValue(callbackId, SAVE_SUCCESS_SECURE_STORAGE)
            }
            val onFailed = { error: MiniAppStorageError ->
                bridgeExecutor.postError(callbackId, Gson().toJson(error))
            }
            if (storageState != StorageState.LOCK)
                secureStorage.insertSecureStorageItem(
                    miniAppId,
                    callbackObj.param.secureStorageItems,
                    onSuccess,
                    onFailed
                )
            else
                bridgeExecutor.postError(
                    callbackId,
                    Gson().toJson(MiniAppStorageError.storageOccupiedError)
                )
        } else {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    fun onGetItem(callbackId: String, jsonStr: String) = whenReady {
        val callbackObj: GetItemCallbackObj? =
            Gson().fromJson(jsonStr, GetItemCallbackObj::class.java)
        if (callbackObj != null) {
            if (!cachedItems.isNullOrEmpty() && cachedItems!!.containsKey(callbackObj.param.secureStorageKey)) {
                bridgeExecutor.postValue(callbackId, cachedItems!![callbackObj.param.secureStorageKey] ?: "")
            } else {
                val onSuccess = { itemValue: String ->
                    bridgeExecutor.postValue(callbackId, itemValue)
                }
                val onFailed = { error: MiniAppStorageError ->
                    bridgeExecutor.postError(callbackId, Gson().toJson(error))
                }
                secureStorage.getItem(miniAppId, callbackObj.param.secureStorageKey, onSuccess, onFailed)
            }
        } else {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    fun onRemoveItems(callbackId: String, jsonStr: String) = whenReady {
        val callbackObj: DeleteItemsCallbackObj? =
            Gson().fromJson(jsonStr, DeleteItemsCallbackObj::class.java)
        if (callbackObj != null) {
            val onSuccess = { items: Map<String, String> ->
                cachedItems = items
                bridgeExecutor.postValue(callbackId, REMOVE_ITEMS_SUCCESS_SECURE_STORAGE)
            }
            val onFailed = { error: MiniAppStorageError ->
                bridgeExecutor.postError(callbackId, Gson().toJson(error))
            }
            if (storageState != StorageState.LOCK)
                secureStorage.deleteSecureStorageItems(
                    miniAppId,
                    callbackObj.param.secureStorageKeyList,
                    onSuccess,
                    onFailed
                )
            else
                bridgeExecutor.postError(
                    callbackId,
                    Gson().toJson(MiniAppStorageError.storageOccupiedError)
                )
        } else {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    fun onClearAll(callbackId: String) = whenReady {
        val onSuccess = {
            bridgeExecutor.postValue(callbackId, REMOVE_SUCCESS_SECURE_STORAGE)
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(error))
        }
        secureStorage.deleteSecureStorage(miniAppId, onSuccess, onFailed)
    }

    fun onSize(callbackId: String) = whenReady {
        val onSuccess = { fileSize: Double ->
            val storageSize = Gson().toJson(MiniAppSecureStorageSize(fileSize, storageMaxSizeKB.toDouble()))
            bridgeExecutor.postValue(callbackId, storageSize)
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(error))
        }
        secureStorage.secureStorageSize(miniAppId, onSuccess, onFailed)
    }

    fun onClearSecureStorage() {
        cachedItems = null
        secureStorage.storageState.removeObserver(stateObserver)
    }

    internal companion object {
        const val ERR_WRONG_JSON_FORMAT = "Can not parse secure storage json object"
        const val SAVE_SUCCESS_SECURE_STORAGE = "Items saved successfully."
        const val REMOVE_ITEMS_SUCCESS_SECURE_STORAGE = "Items removed successfully."
        const val REMOVE_SUCCESS_SECURE_STORAGE = "Storage removed successfully."
    }
}
