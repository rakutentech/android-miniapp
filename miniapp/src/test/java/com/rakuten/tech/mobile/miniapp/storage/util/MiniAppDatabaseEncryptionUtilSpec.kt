package com.rakuten.tech.mobile.miniapp.storage.util

import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.rakuten.tech.mobile.miniapp.JAVA7_SECRET_KEY_ALGORITHM
import com.rakuten.tech.mobile.miniapp.JAVA8_SECRET_KEY_ALGORITHM
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import javax.crypto.spec.IvParameterSpec
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class MiniAppDatabaseEncryptionUtilSpec {

    @Test
    fun `verify it returns the SHA1 algorithm for SDK below Android 7`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 24)

        val key = MiniAppDatabaseEncryptionUtil.getSecretKeyAlgorithm()

        assertEquals(key.algorithm, JAVA7_SECRET_KEY_ALGORITHM)
        assertNotEquals(key.algorithm, JAVA8_SECRET_KEY_ALGORITHM)
    }

    @Test
    fun `verify it returns the SHA256 algorithm for SDK above Android 7`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 25)

        val key = MiniAppDatabaseEncryptionUtil.getSecretKeyAlgorithm()

        assertEquals(key.algorithm, JAVA8_SECRET_KEY_ALGORITHM)
        assertNotEquals(key.algorithm, JAVA7_SECRET_KEY_ALGORITHM)
    }

    @Test
    fun `encrypt the given passcode with SHA1 algorithm it should match after the decryption`() {
        val saltByteLength = 8
        val passcode = TEST_MA_ID
        val encryptionAlgorithm = "AES"

        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 24)

        val salt = MiniAppDatabaseEncryptionUtil.generateSalt()
        val iv: IvParameterSpec = MiniAppDatabaseEncryptionUtil.generateIv()
        val secretKey = MiniAppDatabaseEncryptionUtil.getSecretKeyFromPassword(passcode, salt)
        val encryptedPasscode = MiniAppDatabaseEncryptionUtil.encrypt(passcode, secretKey, iv)

        val stringIv = Base64.encodeToString(iv.iv, Base64.DEFAULT)
        val byteIv = Base64.decode(stringIv, Base64.DEFAULT)
        val decryptedPasscode = MiniAppDatabaseEncryptionUtil.decrypt(
            encryptedPasscode,
            secretKey,
            IvParameterSpec(byteIv)
        )

        assertEquals(passcode, decryptedPasscode)
        assertTrue((salt.size == saltByteLength))
        assertEquals(secretKey.algorithm, encryptionAlgorithm)
    }

    @Test
    fun `encrypt the given passcode with SHA256 algorithm it should match after the decryption`() {
        val saltByteLength = 8
        val passcode = TEST_MA_ID
        val encryptionAlgorithm = "AES"

        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 25)

        val salt = MiniAppDatabaseEncryptionUtil.generateSalt()
        val iv: IvParameterSpec = MiniAppDatabaseEncryptionUtil.generateIv()
        val secretKey = MiniAppDatabaseEncryptionUtil.getSecretKeyFromPassword(passcode, salt)
        val encryptedPasscode = MiniAppDatabaseEncryptionUtil.encrypt(passcode, secretKey, iv)

        val stringIv = Base64.encodeToString(iv.iv, Base64.DEFAULT)
        val byteIv = Base64.decode(stringIv, Base64.DEFAULT)
        val decryptedPasscode = MiniAppDatabaseEncryptionUtil.decrypt(
            encryptedPasscode,
            secretKey,
            IvParameterSpec(byteIv)
        )

        assertEquals(passcode, decryptedPasscode)
        assertTrue((salt.size == saltByteLength))
        assertEquals(secretKey.algorithm, encryptionAlgorithm)
    }

    @Test
    fun `encryptPasscode should return a string`() {
        MiniAppDatabaseEncryptionUtil.encryptPasscode(getApplicationContext(), TEST_MA_ID)
            .shouldBeInstanceOf<String>()
    }

    @Test
    fun `decryptPasscode should return a string`() {
        MiniAppDatabaseEncryptionUtil.decryptPasscode(getApplicationContext(), TEST_MA_ID)
            .shouldBeInstanceOf<String>()
    }

    @Test
    fun `getPasscodeHolder should return null when empty passcode`() {
        val context: Context = getApplicationContext()
        val prefs = context.getSharedPreferences("MiniAppDatabase", Context.MODE_PRIVATE)
        prefs.edit().putString("PASSCODE", "").apply()

        MiniAppDatabaseEncryptionUtil.getPasscodeHolder(context) shouldBe null
    }
}
