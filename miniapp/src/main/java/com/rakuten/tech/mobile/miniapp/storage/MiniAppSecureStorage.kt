package com.rakuten.tech.mobile.miniapp.storage

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets

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
            } ?: kotlin.run {
                writeToEncryptedFile(miniAppId, serializeItems(items))
            }
        } else {
            writeToEncryptedFile(miniAppId, serializeItems(items))
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

