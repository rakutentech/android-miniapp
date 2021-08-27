package com.rakuten.tech.mobile.miniapp.signatureverifier.verification

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.rakuten.tech.mobile.miniapp.signatureverifier.SignatureVerifier
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@SuppressWarnings("TooGenericExceptionCaught")
internal class AesEncryptor @VisibleForTesting constructor(
        private val keyStore: KeyStore?,
        private val keyGenerator: AesKeyGenerator
) {

    constructor() : this(
            keyStore = try {
                KeyStore.getInstance("AndroidKeyStore")
            } catch (ke: KeyStoreException) {
                Log.d(TAG, "Error generating keystore", ke)
                null
            },
            keyGenerator = AesKeyGenerator(alias = KEYSTORE_ALIAS, provider = "AndroidKeyStore")
    )

    private val encryptionKey
        get() = (
                try {
                    keyStore?.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry?
                } catch (e: ClassCastException) {
                    // Key wasn't an AES key, so we need to generate a new one
                    Log.d(TAG, "Error retrieving key from KeyStore, will be generate new key.", e)
                    SignatureVerifier.callback?.let { it(e) }
                    null
                }
                )?.secretKey ?: keyGenerator.generateKey()

    init {
        try {
            keyStore?.load(null)
        } catch (ex: Exception) {
            Log.d(TAG, "Error loading the keystore", ex)
            SignatureVerifier.callback?.let { it(ex) }
        }
    }

    fun encrypt(data: String, cipher: Cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)): String? {
        val key = encryptionKey // so key will only be generated once call
        if (key != null) {
            try {
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

                return AesEncryptedData(
                        Base64.encodeToString(cipher.iv, Base64.DEFAULT),
                        Base64.encodeToString(encryptedData, Base64.DEFAULT)
                ).toJsonString()
            } catch (e: Exception) {
                Log.d(TAG, "Error encrypting the data", e)
                SignatureVerifier.callback?.let { it(e) }
            }
        }

        return null
    }

    fun decrypt(data: String, cipher: Cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)): String? {
        val key = encryptionKey // so key will only be generated once per call
        if (key != null) {
            try {
                val (iv, encryptedData) = AesEncryptedData.fromJsonString(data)

                val spec = GCMParameterSpec(GCM_TAG_LENGTH, Base64.decode(iv, Base64.DEFAULT))
                cipher.init(Cipher.DECRYPT_MODE, key, spec)

                val decryptedData = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))

                return String(decryptedData)
            } catch (e: Exception) {
                Log.d(TAG, "Error decrypting the data", e)
                SignatureVerifier.callback?.let { it(e) }
            }
        }

        return null
    }

    companion object {
        private const val TAG = "RSV_AES"
        private const val KEYSTORE_ALIAS = "signature-verifier-public-key-encryption-decryption"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }
}

@RequiresApi(Build.VERSION_CODES.M)
internal class AesKeyGenerator(
        private val alias: String,
        private val provider: String
) {

    @SuppressWarnings("TooGenericExceptionCaught")
    fun generateKey(): SecretKey? {
        return try {
            val algorithmSpec = KeyGenParameterSpec.Builder(alias, PURPOSE)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()

            val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, provider)

            keyGenerator.init(algorithmSpec)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            Log.d(TAG, "Error generating the secret key", e)
            SignatureVerifier.callback?.let { it(e) }
            null
        }
    }

    companion object {
        private const val TAG = "RSV_AesKeyGenerator"
        private const val PURPOSE = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    }
}

internal data class AesEncryptedData(
        val iv: String,
        val encryptedData: String
) {

    fun toJsonString(): String = Gson().toJson(this)

    companion object {
        private const val TAG = "RSV_AesEncryptedData"
        fun fromJsonString(body: String): AesEncryptedData = try {
            Gson().fromJson(body, AesEncryptedData::class.java)
        } catch (ex: JsonParseException) {
            Log.d(TAG, ex.localizedMessage, ex)
            SignatureVerifier.callback?.let { it(ex) }
            AesEncryptedData("", "")
        }
    }
}
