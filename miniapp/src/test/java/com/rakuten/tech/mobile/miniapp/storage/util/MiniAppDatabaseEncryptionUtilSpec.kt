package com.rakuten.tech.mobile.miniapp.storage.util

import android.util.Base64
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.crypto.spec.IvParameterSpec
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class MiniAppDatabaseEncryptionUtilSpec {

    @Test
    fun `encrypt the given passcode and it should match after the decryption`() {
        val passcode = TEST_MA_ID
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
    }

    @Test
    fun `encryptPasscode should return a string`() {
        MiniAppDatabaseEncryptionUtil.encryptPasscode(getApplicationContext(), TEST_MA_ID)
            .shouldBeInstanceOf<String>()
    }
}
