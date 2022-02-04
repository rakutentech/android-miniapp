package com.rakuten.tech.mobile.miniapp.signatureverifier

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.miniapp.RobolectricBaseSpec
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import com.rakuten.tech.mobile.miniapp.signatureverifier.verification.PublicKeyCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi
open class SignatureVerifierSpec : RobolectricBaseSpec() {

    companion object {

        private const val KEY_ID = "test_id"
        private const val PUBLIC_KEY =
            "BI2zZr56ghnMLXBMeC4bkIVg6zpFD2ICIS7V6cWo8p8LkibuershO+Hd5ru6oBFLlUk" +
                    "6IFFOIVfHKiOenHLBNIY="
        private const val BODY = """{"testKey": "test_value"}"""
        private const val SIGNATURE =
            "MEUCIHRXIgQhyASpyCP1Lg0ZSn2/bUbTq6U7jpKBa9Ow/1OTAiEA4jAq48uDgNl7UM7" +
                    "LmxhiRhPPNnTolokScTq5ijbp5fU="

        private const val BASE_URL = "http://sample.com"
        private const val SUB_KEY = "test_key"
    }

    private val mockPublicKeyCache = Mockito.mock(PublicKeyCache::class.java)
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val function: (ex: Exception) -> Unit = {}
    private val mockCb = Mockito.mock(function.javaClass)

    @Before
    fun setup() {
        When calling mockPublicKeyCache[KEY_ID] itReturns PUBLIC_KEY
    }

    @Test
    fun `should initialize instance with correct parameters`() {
        val instance = SignatureVerifier.init(context, BASE_URL, SUB_KEY)

        instance.shouldNotBeNull()
        instance shouldBeInstanceOf SignatureVerifier::class
    }

    @Test
    fun `should initialize instance with correct parameters with callback`() {
        val instance = SignatureVerifier.init(context, BASE_URL, SUB_KEY)

        instance.shouldNotBeNull()
    }

    @Test
    fun `should return null initialization failed due to context`() {
        val mockContext = Mockito.mock(Context::class.java)
        SignatureVerifier.init(mockContext, BASE_URL, SUB_KEY).shouldBeNull()
    }

    @Test
    fun `should return null initialization failed due to invalid endpoint`() {
        SignatureVerifier.init(context, "", SUB_KEY).shouldBeNull()
    }

    @Test
    fun `should return null initialization failed due to invalid endpoint with callback`() {
        SignatureVerifier.init(context, "", SUB_KEY).shouldBeNull()
    }

    @Test
    fun `should return null initialization failed due to invalid key`() {
        SignatureVerifier.init(context, BASE_URL, "").shouldBeNull()
    }

    @Test
    fun `should return null initialization failed due to invalid key with callback`() {
        SignatureVerifier.init(context, BASE_URL, "").shouldBeNull()
    }

    @Test
    fun `should not verify the signature when message has been modified`() = runBlockingTest {
        val verifier = SignatureVerifier(mockPublicKeyCache, mock(), TestCoroutineDispatcher())

        verifier.verify(
            KEY_ID,
            TEST_MA_VERSION_ID,
            "wrong message".byteInputStream(),
            SIGNATURE
        ) shouldBeEqualTo false
    }

    @Test
    fun `should not verify the signature cache returns null`() = runBlockingTest {
        When calling mockPublicKeyCache[KEY_ID] itReturns null
        val verifier = SignatureVerifier(mockPublicKeyCache, mock(), TestCoroutineDispatcher())

        verifier.verify(
            KEY_ID,
            TEST_MA_VERSION_ID,
            "wrong message".byteInputStream(),
            SIGNATURE
        ) shouldBeEqualTo false
    }
}
