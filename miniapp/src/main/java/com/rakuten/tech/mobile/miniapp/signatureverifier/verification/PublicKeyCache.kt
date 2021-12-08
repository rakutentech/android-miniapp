package com.rakuten.tech.mobile.miniapp.signatureverifier.verification

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.signatureverifier.PublicKeyFetcher
import java.io.File

@SuppressWarnings("ParameterListWrapping")
internal class PublicKeyCache(
    private val keyFetcher: PublicKeyFetcher,
    private val context: Context,
    private var baseUrl: String
) {
    private val testKeys: MutableMap<String, String>? = null
    private val encryptor = AesEncryptor()

    private val file: File by lazy {
        // replace all non-alphanumeric characters to '.':
        // - multiple/group of non-alphanumeric will be replace by one '.'
        // - trailing period is removed
        baseUrl += "keys/"
        val filepath = baseUrl.replace(Regex("[^a-zA-Z0-9]+"), ".").replace(Regex(".$"), "")
        File(context.noBackupFilesDir, ("signature.keys.$filepath").take(MAX_PATH))
    }

    @VisibleForTesting
    internal val keys: MutableMap<String, String> by lazy {
        testKeys ?: if (file.exists()) {
            val text = file.readText()

            if (text.isNotBlank()) {
                val type = object : TypeToken<MutableMap<String, String>>() {}.type
                Gson().fromJson(text, type)
            } else {
                mutableMapOf()
            }
        } else {
            mutableMapOf()
        }
    }

    suspend fun getKey(keyId: String): String? {
        val encryptedKey = keys[keyId]

        return if (encryptedKey != null) {
            encryptor.decrypt(encryptedKey) ?: fetch(keyId)
        } else {
            fetch(keyId)
        }
    }

    fun remove(keyId: String) {
        keys.remove(keyId)

        file.writeText(Gson().toJson(keys))
    }

    @SuppressWarnings("TooGenericExceptionCaught", "PrintStackTrace")
    private suspend fun fetch(keyId: String): String? {
        val key = try {
            keyFetcher.fetch(keyId)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
        if (!key.isNullOrEmpty()) {
            encryptor.encrypt(key)?.let {
                keys[keyId] = it
                file.writeText(Gson().toJson(keys))
            }
        }

        return key
    }

    companion object {
        private const val MAX_PATH = 100
    }
}
