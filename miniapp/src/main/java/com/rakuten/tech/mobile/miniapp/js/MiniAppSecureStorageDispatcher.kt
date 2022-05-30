package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage

@Suppress("TooManyFunctions", "LargeClass")
internal class MiniAppSecureStorageDispatcher(
    private val storageMaxSizeKB: Int
) {
    private val databaseVersion = 1
    private lateinit var miniAppId: String
    private lateinit var activity: Activity
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    @VisibleForTesting
    internal lateinit var onSuccess: () -> Unit
    @VisibleForTesting
    internal lateinit var onFailed: (MiniAppSecureStorageError) -> Unit
    @VisibleForTesting
    internal lateinit var onSuccessGetItem: (String) -> Unit
    @VisibleForTesting
    internal lateinit var onSuccessDBSize: (Long) -> Unit

    private val databasesCreatedForMiniAppsSet: MutableSet<String> = HashSet()

    private var miniAppSecuredStorage: MutableMap<String, MiniAppSecureStorage> = HashMap()

    private fun initOrGetSecureStorage(miniAppId: String) : MiniAppSecureStorage {
        return miniAppSecuredStorage.putIfAbsent(
            miniAppId,
            MiniAppSecureStorage(activity, databaseVersion, storageMaxSizeKB)
        ) ?: MiniAppSecureStorage(activity, databaseVersion, storageMaxSizeKB)
    }

    fun setBridgeExecutor(activity: Activity, bridgeExecutor: MiniAppBridgeExecutor) {
        this.activity = activity
        this.bridgeExecutor = bridgeExecutor
    }

    fun setMiniAppComponents(miniAppId: String) {
        this.miniAppId = miniAppId
        this.databasesCreatedForMiniAppsSet.add(miniAppId)
        initOrGetSecureStorage(miniAppId)
    }

    @Suppress("ComplexCondition")
    private fun <T> whenReady(callback: () -> T) {
        if (this::bridgeExecutor.isInitialized &&
            this::activity.isInitialized &&
            this::miniAppId.isInitialized
            //this::secureStorage.isInitialized
        ) {
            callback.invoke()
        }
    }

    fun onLoad() = whenReady {
        onSuccess = {
            bridgeExecutor.dispatchEvent(eventType = NativeEventType.MINIAPP_SECURE_STORAGE_READY.value)
        }
        onFailed = { errorSecure: MiniAppSecureStorageError ->
            bridgeExecutor.dispatchEvent(
                eventType = NativeEventType.MINIAPP_SECURE_STORAGE_LOAD_ERROR.value,
                value = Gson().toJson(errorSecure)
            )
        }
        initOrGetSecureStorage(miniAppId).load(miniAppId, onSuccess, onFailed)
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException", "ComplexMethod", "LongMethod")
    fun onSetItems(callbackId: String, jsonStr: String) = whenReady {
        try {
            val callbackObj: SecureStorageCallbackObj? =
                Gson().fromJson(jsonStr, SecureStorageCallbackObj::class.java)
            if (callbackObj != null) {
                onSuccess = {
                    bridgeExecutor.postValue(callbackId, SAVE_SUCCESS_SECURE_STORAGE)
                }
                onFailed = { errorSecure: MiniAppSecureStorageError ->
                    bridgeExecutor.postError(callbackId, Gson().toJson(errorSecure))
                }
                initOrGetSecureStorage(miniAppId).insertItems(
                    miniAppId,
                    callbackObj.param.secureStorageItems,
                    onSuccess,
                    onFailed
                )
            } else {
                bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun onGetItem(callbackId: String, jsonStr: String) = whenReady {
        try {
            val callbackObj: GetItemCallbackObj? =
                Gson().fromJson(jsonStr, GetItemCallbackObj::class.java)
            if (callbackObj != null) {
                onSuccessGetItem = { itemValue: String ->
                    bridgeExecutor.postValue(callbackId, itemValue)
                }
                onFailed = { errorSecure: MiniAppSecureStorageError ->
                    bridgeExecutor.postError(callbackId, Gson().toJson(errorSecure))
                }
                initOrGetSecureStorage(miniAppId).getItem(
                    miniAppId,
                    callbackObj.param.secureStorageKey,
                    onSuccessGetItem,
                    onFailed
                )
            } else {
                bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun onRemoveItems(callbackId: String, jsonStr: String) = whenReady {
        try {
            val callbackObj: DeleteItemsCallbackObj? =
                Gson().fromJson(jsonStr, DeleteItemsCallbackObj::class.java)
            if (callbackObj != null) {
                onSuccess = {
                    bridgeExecutor.postValue(callbackId, REMOVE_ITEMS_SUCCESS_SECURE_STORAGE)
                }
                onFailed = { errorSecure: MiniAppSecureStorageError ->
                    bridgeExecutor.postError(callbackId, Gson().toJson(errorSecure))
                }
                initOrGetSecureStorage(miniAppId).deleteItems(
                    miniAppId,
                    callbackObj.param.secureStorageKeyList,
                    onSuccess,
                    onFailed
                )
            } else {
                bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    fun onClearAll(callbackId: String) = whenReady {
        onSuccess = {
//            miniAppSecuredStorage.remove(miniAppId)
            bridgeExecutor.postValue(callbackId, REMOVE_SUCCESS_SECURE_STORAGE)
        }
        onFailed = { errorSecure: MiniAppSecureStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(errorSecure))
        }
        initOrGetSecureStorage(miniAppId).delete(miniAppId, onSuccess, onFailed)
    }

    @Suppress("MagicNumber")
    @Deprecated("No Longer Needed")
    fun onSize(callbackId: String) = whenReady {
        onSuccessDBSize = { fileSize: Long ->
            val maxSizeInBytes = storageMaxSizeKB * 1024
            val storageSize =
                Gson().toJson(MiniAppSecureStorageSize(fileSize, maxSizeInBytes.toLong()))
            bridgeExecutor.postValue(callbackId, storageSize)
        }
        initOrGetSecureStorage(miniAppId).getDatabaseUsedSize(miniAppId, onSuccessDBSize)
    }

    fun cleanupSecureStorage() {}

    /**
     * Will be invoked by MiniApp.clearSecureStorage(miniAppId: String).
     * @param miniAppId will be used to find the storage to be deleted.
     */
    fun clearSecureStorage(miniAppId: String) = whenReady {
        initOrGetSecureStorage(miniAppId).clearSecureStorage(miniAppId)
        miniAppSecuredStorage.remove(miniAppId)
    }

    /**
     * Will be invoked by MiniApp.clearSecureStorage.
     */
    fun clearSecureStorage() = whenReady {
        databasesCreatedForMiniAppsSet.forEach {
            clearSecureStorage(it)
        }
    }

    internal companion object {
        const val SAVE_SUCCESS_SECURE_STORAGE = "Items saved successfully."
        const val REMOVE_SUCCESS_SECURE_STORAGE = "Storage removed successfully."
        const val REMOVE_ITEMS_SUCCESS_SECURE_STORAGE = "Items removed successfully."
        const val ERR_WRONG_JSON_FORMAT = "Can not parse secure storage json object."
    }
}
