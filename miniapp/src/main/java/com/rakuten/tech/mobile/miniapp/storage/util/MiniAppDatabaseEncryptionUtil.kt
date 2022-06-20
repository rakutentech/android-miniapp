package com.rakuten.tech.mobile.miniapp.storage.util

import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val ENCRYPTION_ALGORITHM = "AES"
private const val SHARED_PREFERENCE_KEY = "PASSCODE"
private const val SHARED_PREFERENCE_NAME = "MiniAppDatabase"
private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding"
private const val SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256"

/**
 * Container for everything needed for decrypting the database.
 *
 * @param iv initialization vector
 * @param salt cryptographic salt
 * @param encryptedPasscode encrypted database key
 */
@Keep
private data class EncryptedPasscodeDataHolder(
    val iv: String,
    val salt: String,
    val encryptedPasscode: String
)

private const val PASSCODE_DECRYPTION_ERROR = "Failed to decrypt database key"

/**
 * Singleton Database Encryption Util Class
 * Using AES/CBC/PKCS5Padding algorithm to encrypt the
 * database passcode and the encrypted passcode will be
 * stored to the preferences to pass the same encrypted
 * passcode lock everytime to lock the access of the
 * database from any third party database browser app.
 *
 * If needed in the future then this passcode can be taken from
 * the user with a enter passcode UI screen to access the Database.
 */
internal object MiniAppDatabaseEncryptionUtil {

    @VisibleForTesting
    @SuppressWarnings("MagicNumber")
    internal fun getSecretKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        return SecretKeySpec(
            factory.generateSecret(spec)
                .encoded, ENCRYPTION_ALGORITHM
        )
    }

    @VisibleForTesting
    @SuppressWarnings("MagicNumber")
    internal fun generateIv(): IvParameterSpec {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }

    @VisibleForTesting
    @SuppressWarnings("MagicNumber")
    internal fun generateSalt(): ByteArray {
        val salt = ByteArray(8).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SecureRandom.getInstanceStrong().nextBytes(this)
            } else {
                SecureRandom().nextBytes(this)
            }
        }
        return salt
    }

    @SuppressWarnings("SwallowedException")
    private fun getPasscodeHolder(context: Context): EncryptedPasscodeDataHolder? {
        val prefs = context.getSharedPreferences(
            SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )

        val serialized = prefs.getString(SHARED_PREFERENCE_KEY, null)
        if (serialized.isNullOrBlank()) {
            return null
        }

        return try {
            Gson().fromJson(serialized,
                object : TypeToken<EncryptedPasscodeDataHolder>() {}.type
            )
        } catch (ex: JsonSyntaxException) {
            null
        }
    }

    private fun saveToPreferences(context: Context, holder: EncryptedPasscodeDataHolder) {
        val serialized = Gson().toJson(holder)
        val prefs = context.getSharedPreferences(
            SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit().putString(SHARED_PREFERENCE_KEY, serialized).apply()
    }

    @VisibleForTesting
    internal fun encrypt(
        passcode: String,
        key: SecretKey,
        iv: IvParameterSpec
    ): String {
        val cipher: Cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val encryptedPasscode: ByteArray = cipher.doFinal(passcode.toByteArray())
        print("##### encryptedPasscode = $encryptedPasscode")
        return Base64.encodeToString(encryptedPasscode, Base64.DEFAULT)
    }

    @VisibleForTesting
    internal fun decrypt(
        encryptedPasscode: String,
        key: SecretKey,
        iv: IvParameterSpec
    ): String {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val decryptedPasscode = cipher.doFinal(
            Base64.decode(encryptedPasscode, Base64.DEFAULT)
        )
        return String(decryptedPasscode)
    }

    @Suppress("ComplexCondition")
    internal fun encryptPasscode(context: Context, passcode: String): String {

        val holder = getPasscodeHolder(context)

        if (holder == null || (holder.salt == null || holder.iv == null || holder.encryptedPasscode == null)) {
            val salt = generateSalt()
            val iv: IvParameterSpec = generateIv()
            val secretKey = getSecretKeyFromPassword(passcode, salt)
            val encryptedPasscode = encrypt(passcode, secretKey, iv)
            val holder = EncryptedPasscodeDataHolder(
                Base64.encodeToString(iv.iv, Base64.DEFAULT),
                Base64.encodeToString(salt, Base64.DEFAULT),
                encryptedPasscode
            )
            saveToPreferences(context, holder)
            return encryptedPasscode
        }
        return holder.encryptedPasscode
    }

    /**
     * Kept For the future reference in case in the future
     * if the passcode is asked from the user to access DB
     * in that case we'll decrypt the encrypted passcode
     * to match with the given passcode.
     */
    private fun decryptPasscode(context: Context, passcode: String): String {
        val holder = getPasscodeHolder(context)
        if (holder != null) {
            val iv = Base64.decode(holder.iv, Base64.DEFAULT)
            val salt = Base64.decode(holder.salt, Base64.DEFAULT)
            val encryptedPasscode = holder.encryptedPasscode
            val secretKey: SecretKey = getSecretKeyFromPassword(passcode, salt)

            return decrypt(encryptedPasscode, secretKey, IvParameterSpec(iv))
        }
        return PASSCODE_DECRYPTION_ERROR
    }
}
