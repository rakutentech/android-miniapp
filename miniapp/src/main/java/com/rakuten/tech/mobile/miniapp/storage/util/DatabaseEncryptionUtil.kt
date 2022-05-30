package com.rakuten.tech.mobile.miniapp.storage.util

import android.content.Context
import android.os.Build
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.security.AlgorithmParameters
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Container for everything needed for decrypting the database.
 *
 * @param iv initialization vector
 * @param key encrypted database key
 * @param salt cryptographic salt
 */
private data class EncryptedPasscodeHolder(
    val iv: String,
    val key: String,
    val salt: String
)

private const val IV = "bVQzNFNhRkQ1Njc4UUFaWA=="
private const val SHARED_PREFERENCE_KEY = "PASSCODE"
private const val SHARED_PREFERENCE_NAME = "MiniAppDatabase"
private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
private const val ENCRYPTION_ALGORITHM = "PBKDF2WithHmacSHA256"

/**
 * Singleton Reference
 * Every MiniApp Database will be secured with a passcode
 * and the passcode will be encrypted with a randomly
 * generated key and saved to the shared preference.
 */
object DatabaseEncryptionUtil {
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    private var rawByteKey: ByteArray? = null
    private var encryptedPasscode: String? = null

    /**
     * An extension function that converts a ByteArray to a hex encoded String
     */
    private fun ByteArray.toHex(): String {
        val result = StringBuilder()
        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }
        return result.toString()
    }

    /**
     * Returns the database key suitable for using with Room.
     *
     * @param passcode the user's passcode
     * @param context the caller's context
     */
    fun encryptDatabasePasscode(context: Context, passcode: String): String? {
        if (encryptedPasscode == null) {
            initKey(context, passcode.toCharArray())
        }
        return encryptedPasscode
    }

    private fun initKey(context: Context, passcode: CharArray) {
        val holder = getPasscodeHolder(context)
        if (holder == null) {
            createNewKey()
            encryptAndPersistPasscode(context, passcode)
        } else {
            rawByteKey = decryptPasscode(passcode, holder)
            encryptedPasscode = rawByteKey!!.toHex()
        }
    }

    /**
     * Generates a new database key.
     */
    private fun createNewKey() {
        // This is the raw key that we'll be encrypting + storing
        rawByteKey = generateRandomKey()
        // This is the key that will be used by Database
        encryptedPasscode = rawByteKey!!.toHex()
    }

    /**
     * Generates a random 32 byte key.
     *
     * @return a byte array containing random values
     */
    private fun generateRandomKey(): ByteArray =
        ByteArray(32).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SecureRandom.getInstanceStrong().nextBytes(this)
            } else {
                SecureRandom().nextBytes(this)
            }
        }

    /**
     * Retrieves the [EncryptedPasscodeHolder] instance from prefs.
     *
     * @param context the caller's context
     * @return the storable instance
     */
    private fun getPasscodeHolder(context: Context): EncryptedPasscodeHolder? {
        val prefs = context.getSharedPreferences(
            SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE)

        val serialized = prefs.getString(SHARED_PREFERENCE_KEY, null)
        if (serialized.isNullOrBlank()) {
            return null
        }

        return try {
            Gson().fromJson(serialized,
                object: TypeToken<EncryptedPasscodeHolder>() {}.type)
        } catch (ex: JsonSyntaxException) {
            null
        }
    }

    private fun encryptAndPersistPasscode(context: Context, userPasscode: CharArray) {
        val encryptedPasscode = rawByteKey?.let { encryptPasscode(it, userPasscode) }
        // Implementation explained in next step
        if (encryptedPasscode != null) {
            saveToPreferences(context, encryptedPasscode)
        }
    }

    /**
     * Save the storable instance to preferences.
     *
     * @param storable a storable instance
     */
    private fun saveToPreferences(context: Context, storable: EncryptedPasscodeHolder) {
        val serialized = Gson().toJson(storable)
        val prefs = context.getSharedPreferences(
            SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE)
        prefs.edit().putString(SHARED_PREFERENCE_KEY, serialized).apply()
    }

    private fun generateSecretKey(passcode: CharArray, salt: ByteArray): SecretKey {
        // Initialize PBE with password
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(passcode, salt, 65536, 256)
        val tmp: SecretKey = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    private fun generateIv(): IvParameterSpec? {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }

    /**
     * Returns a [EncryptedPasscodeHolder] instance with the db key encrypted using PBE.
     *
     * @param rawDbKey the raw database key
     * @param userPasscode the user's passcode
     * @return storable instance
     */
    private fun encryptPasscode(
        rawDbKey: ByteArray,
        userPasscode: CharArray
    ): EncryptedPasscodeHolder {

        // Generate a random 8 byte salt
        val salt = ByteArray(8).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SecureRandom.getInstanceStrong().nextBytes(this)
            } else {
                SecureRandom().nextBytes(this)
            }
        }
        val secret: SecretKey = generateSecretKey(userPasscode, salt)
        // Now encrypt the database key with PBE
        val cipher: Cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secret)
        val params: AlgorithmParameters = cipher.parameters
        //val iv: ByteArray = params.getParameterSpec(IvParameterSpec::class.java).iv // Not working
        val iv: ByteArray = generateIv()?.iv ?: IV.toByteArray()
        /**
         * Using some random static iv
         * Since the above code isn't working
         * to fetch the iv.
         * TODO: Debug above code and fetch dynamic iv
         */
        //val iv = IV.toByteArray()
        val ciphertext: ByteArray = cipher.doFinal(rawDbKey)

        // Return the IV and CipherText which can be stored to disk
        return EncryptedPasscodeHolder(
            Base64.encodeToString(iv, Base64.DEFAULT),
            Base64.encodeToString(ciphertext, Base64.DEFAULT),
            Base64.encodeToString(salt, Base64.DEFAULT)
        )
    }

    /**
     * Decrypts the [EncryptedPasscodeHolder] instance using the [passcode].
     *
     * @pararm passcode the user's passcode
     * @param storable the storable instance previously saved with [saveToPreferences]
     * @return the raw byte key previously generated with [generateRandomKey]
     */
    private fun decryptPasscode(passcode: CharArray, storable: EncryptedPasscodeHolder): ByteArray {
        val aesWrappedKey = Base64.decode(storable.key, Base64.DEFAULT)
        val iv = Base64.decode(storable.iv, Base64.DEFAULT)
        val salt = Base64.decode(storable.salt, Base64.DEFAULT)
        val secret: SecretKey = generateSecretKey(passcode, salt)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secret, IvParameterSpec(iv))
        return cipher.doFinal(aesWrappedKey)
    }
}
