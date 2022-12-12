package com.rakuten.tech.mobile.miniapp.signatureverifier.verification

import com.google.gson.JsonParseException
import com.rakuten.tech.mobile.miniapp.RobolectricBaseSpec
import com.rakuten.tech.mobile.miniapp.TEST_DATA
import org.amshove.kluent.*
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.lang.ClassCastException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


open class AesEncryptorSpec : RobolectricBaseSpec() {

    internal val mockKeyStore = Mockito.mock(KeyStore::class.java)
    private val mockKeyGenerator = Mockito.mock(AesKeyGenerator::class.java)
    private val mockKeyEntry = Mockito.mock(KeyStore.SecretKeyEntry::class.java)

    @Before
    open fun setup() {
        val key = generateAesKey()
        When calling mockKeyGenerator.generateKey() itReturns key
        When calling mockKeyEntry.secretKey itReturns key
        When calling mockKeyStore.getEntry(any(), anyOrNull()) itReturns mockKeyEntry
    }

    @Test
    fun `it should encrypt the data`() {
        val encryptor = createAesEncryptor()

        val data = encryptor.encrypt(TEST_DATA)
        data.shouldNotBeNull()
        data.shouldNotContain(TEST_DATA)
    }

    @Test
    fun `it should attach IV`() {
        val encryptor = createAesEncryptor()

        val data = encryptor.encrypt(TEST_DATA)
        data.shouldNotBeNull()
        data.shouldContain("\"iv\":")
    }

    @Test
    fun `it should decrypt the data`() {
        val encryptor = createAesEncryptor()

        val encryptedData = encryptor.encrypt(TEST_DATA)

        encryptedData.shouldNotBeNull()
        encryptor.decrypt(encryptedData)!! shouldBeEqualTo TEST_DATA
    }

    @Test
    fun `it should generate a new key when one is not in the key store`() {
        When calling mockKeyStore.getEntry(any(), anyOrNull()) itReturns null
        val encryptor = createAesEncryptor()

        encryptor.encrypt(TEST_DATA)

        Mockito.verify(mockKeyGenerator).generateKey()
    }

    @Test
    fun `it should not generate a key when one is already in the key store`() {
        When calling mockKeyGenerator.generateKey() itReturns mock()
        val encryptor = createAesEncryptor()

        encryptor.encrypt(TEST_DATA)

        Mockito.verify(mockKeyGenerator, never()).generateKey()
    }

    @Test
    fun `it should generate a new key when the one in the store is not an AES key`() {
        val mockRsaKeyEntry: KeyStore.PrivateKeyEntry = mock()
        When calling mockKeyStore.getEntry(any(), anyOrNull()) itReturns mockRsaKeyEntry
        val encryptor = createAesEncryptor()
        encryptor.encrypt(TEST_DATA)

        Mockito.verify(mockKeyGenerator).generateKey()
    }

    internal fun createAesEncryptor(
        keyStore: KeyStore? = mockKeyStore,
        keyGenerator: AesKeyGenerator = mockKeyGenerator
    ) = AesEncryptor(keyStore, keyGenerator)

    private fun generateAesKey(): SecretKey {
        val kgen = KeyGenerator.getInstance("AES")
        kgen.init(256)
        return kgen.generateKey()
    }
}

open class AesEncryptorExceptionSpec : AesEncryptorSpec() {

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun `should not crash and return valid when keystore is null`() {
        val encryptor = createAesEncryptor(keyStore = null)

        verifyFunction(encryptor, true)
    }

    @Test
    fun `should not crash and return valid when keystore throws ClassCastException`() {
        When calling mockKeyStore.getEntry(any(), anyOrNull()) itThrows ClassCastException()

        val encryptor = createAesEncryptor()

        verifyFunction(encryptor, true)
    }

    @Test
    fun `should not crash and return null when key generator throws IllegalArgumentException`() {
        When calling mockKeyStore.getEntry(any(), anyOrNull()) itReturns null

        val generator = AesKeyGenerator("", "AndroidKeyStore")

        val encryptor = createAesEncryptor(keyGenerator = generator)

        verifyFunction(encryptor, false)
    }

    @Test
    fun `should not crash and return valid when keystore load throw NoSuchAlgorithmException`() {
        When calling mockKeyStore.load(isNull()) itThrows NoSuchAlgorithmException()
        val encryptor = createAesEncryptor()

        verifyFunction(encryptor, true)
    }

    @Test
    fun `should not crash and return null when cipher throws InvalidKeyException`() {
        val mockCipher = Mockito.mock(Cipher::class.java)
        When calling mockCipher.init(eq(Cipher.DECRYPT_MODE), any(SecretKey::class), any(
            GCMParameterSpec::class)) itThrows InvalidKeyException()
        When calling mockCipher.init(eq(Cipher.ENCRYPT_MODE),
            any(SecretKey::class)) itThrows InvalidKeyException()
        val encryptor = createAesEncryptor()

        encryptor.encrypt(TEST_DATA, mockCipher).shouldBeNull()
        encryptor.decrypt(TEST_DATA, mockCipher).shouldBeNull()
    }

    private fun verifyFunction(encryptor: AesEncryptor, isValid: Boolean) {
        if (isValid) {
            val data = encryptor.encrypt(TEST_DATA)
            data.shouldNotBeNull()
            data.shouldNotContain(TEST_DATA)
            encryptor.decrypt(data).shouldNotBeNull()
        } else {
            encryptor.encrypt(TEST_DATA).shouldBeNull()
            encryptor.decrypt(TEST_DATA).shouldBeNull()
        }
    }
}

class AesEncryptedDataSpec : RobolectricBaseSpec() {

    @Test
    fun `should serialize empty string data is correctly`() {
        val json = AesEncryptedData("", "").toJsonString()
        json.shouldNotBeEmpty()

        val data = AesEncryptedData.fromJsonString(json)
        data.iv.shouldBeEmpty()
        data.encryptedData.shouldBeEmpty()
    }

    @Test
    fun `should return correct data`() {
        val data = AesEncryptedData.fromJsonString(DATA)
        data.iv shouldBeEqualTo "test_iv"
        data.encryptedData shouldBeEqualTo "test_data"
    }

    @Test
    fun `should return empty data when parsing failed`() {
        val data = AesEncryptedData.fromJsonString(DATA_FAILED)
        data.iv.shouldBeEmpty()
        data.encryptedData.shouldBeEmpty()
    }

    @Test
    fun `should return empty data when parsing failed with callback`() {
        val function: (ex: Exception) -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)
        val data = AesEncryptedData.fromJsonString(DATA_FAILED)
        data.iv.shouldBeEmpty()
        data.encryptedData.shouldBeEmpty()

        Mockito.verify(mockCallback, times(0)).invoke(any(JsonParseException::class))
    }

    companion object {
        private val DATA =
            """{
                "iv": "test_iv",
                "encryptedData": "test_data"
            }""".trimIndent()

        private val DATA_FAILED =
            """{
                "iv": "test_iv",
                "encryptedData"
            """.trimIndent()
    }
}
