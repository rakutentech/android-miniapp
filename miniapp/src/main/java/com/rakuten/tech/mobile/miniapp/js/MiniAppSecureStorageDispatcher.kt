package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import java.io.*
import java.nio.channels.FileLock
import java.nio.charset.StandardCharsets

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

    fun onSetItems(callbackId: String, jsonStr: String) = whenReady {
        val items: Map<String, String> = mapOf("key1" to "x", "key2" to "y", "key3" to "zz")
        val serializedData: String = Gson().toJson(items)
        //secureStorage.writeToEncryptedFile(miniAppId, serializedData)
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

private const val SUB_DIR_MINIAPP = "miniapp"
private const val SUB_DIR_SECURE_STORAGE = "secure-storage"

internal class MiniAppSecureStorage(private val activity: Activity) {
    private val hostAppBasePath = activity.filesDir
    private val miniAppBasePath
        get() = "$hostAppBasePath/$SUB_DIR_MINIAPP"
    private val secureStorageBasePath
        get() = "$miniAppBasePath/$SUB_DIR_SECURE_STORAGE/"

    private fun makeDirectoryAvailable(){
        val storageDir = File(secureStorageBasePath)
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
    }

    private fun isStorageAvailable(miniAppId: String): Boolean{
        val securedStorageFile = File(secureStorageBasePath, "$miniAppId.txt")
        return securedStorageFile.exists()
    }

    fun secureStorageSize(miniAppId: String): String{
        return if (isStorageAvailable(miniAppId)){
            val sizeInMb = File(secureStorageBasePath, "$miniAppId.txt").length() / (1024.0 * 1024)
            "%.2f".format(sizeInMb)
        }else{
            "0"
        }
    }

    private fun writeToEncryptedFile(miniAppId: String, content: String) = try {
        makeDirectoryAvailable()
        val fileToWrite = File(secureStorageBasePath, "$miniAppId.txt")
        if (fileToWrite.exists()) {
            fileToWrite.delete()
        }
        val encryptedFile = EncryptedFile.Builder(
            activity,
            fileToWrite,
            MasterKey.Builder(activity).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val fileContent = content.toByteArray(StandardCharsets.UTF_8)
        encryptedFile.openFileOutput().apply {
            write(fileContent)
            flush()
            close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    @VisibleForTesting
    fun readFromEncryptedFile(miniAppId: String): Map<String, String>? {
        try {
            if (isStorageAvailable(miniAppId)) {
                val encryptedFile = EncryptedFile.Builder(
                    activity,
                    File(secureStorageBasePath, "$miniAppId.txt"),
                    MasterKey.Builder(activity).setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build(),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()

                val inputStream = encryptedFile.openFileInput()
                val byteArrayOutputStream = ByteArrayOutputStream()
                var nextByte: Int = inputStream.read()
                while (nextByte != -1) {
                    byteArrayOutputStream.write(nextByte)
                    nextByte = inputStream.read()
                }

                val plaintext: ByteArray = byteArrayOutputStream.toByteArray()
                val jsonToRead = plaintext.toString(Charsets.UTF_8)
                return deSerializeItems(jsonToRead)
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun deleteSecureStorage(miniAppId: String){
        if (isStorageAvailable(miniAppId)) {
            File(secureStorageBasePath, "$miniAppId.txt").delete()
        }
    }

    fun deleteSecureStorageItem(miniAppId: String, keySet: Set<String>){
        if (isStorageAvailable(miniAppId)) {
            val storedItems = readFromEncryptedFile(miniAppId)
            storedItems?.let { items ->
                val filterItems = items.filter { !keySet.contains(it.key) }
                writeToEncryptedFile(miniAppId, serializeItems(filterItems))
            }
        }
    }

    fun insertSecureStorageItem(miniAppId: String, items: Map<String, String>){
        if (isStorageAvailable(miniAppId)) {
            val storedItems = readFromEncryptedFile(miniAppId)
            storedItems?.let {
                val allItems = it.toMutableMap().apply { putAll(items) }
                writeToEncryptedFile(miniAppId, serializeItems(allItems))
            }?: kotlin.run {
                writeToEncryptedFile(miniAppId, serializeItems(items))
            }
        }
    }

    private fun deSerializeItems(jsonToRead: String): Map<String, String> {
        return Gson().fromJson(
            jsonToRead,
            object : TypeToken<Map<String, String>>() {}.type
        )
    }

    private fun serializeItems(items: Map<String, String>): String {
        return Gson().toJson(items)
    }
}
