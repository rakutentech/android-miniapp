package com.rakuten.tech.mobile.miniapp.signatureverifier

import com.rakuten.tech.mobile.miniapp.RobolectricBaseSpec
import com.rakuten.tech.mobile.miniapp.signatureverifier.api.PublicKeyFetcher
import com.rakuten.tech.mobile.miniapp.signatureverifier.verification.PublicKeyCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi
open class RealSignatureVerifierSpec : RobolectricBaseSpec() {

    companion object {

        private const val KEY_ID = "test_id"
        private const val PUBLIC_KEY =
            "BI2zZr56ghnMLXBMeC4bkIVg6zpFD2ICIS7V6cWo8p8LkibuershO+Hd5ru6oBFLlUk" +
                    "6IFFOIVfHKiOenHLBNIY="
        private const val BODY = """{"testKey": "test_value"}"""
        private const val SIGNATURE =
            "MEUCIHRXIgQhyASpyCP1Lg0ZSn2/bUbTq6U7jpKBa9Ow/1OTAiEA4jAq48uDgNl7UM7" +
                    "LmxhiRhPPNnTolokScTq5ijbp5fU="
    }

    private val mockPublicKeyCache = Mockito.mock(PublicKeyCache::class.java)
    private val mockFetcher = Mockito.mock(PublicKeyFetcher::class.java)

    @Before
    fun setup() {
        When calling mockPublicKeyCache[KEY_ID] itReturns PUBLIC_KEY
    }

    @Test
    fun `should verify the signature`() = runBlockingTest {
        val verifier = RealSignatureVerifier(mockPublicKeyCache, TestCoroutineDispatcher())

        verifier.verify(
            KEY_ID,
            BODY.byteInputStream(),
            SIGNATURE
        ) shouldEqualTo true
    }

    @Test
    fun `should not verify the signature when message has been modified`() = runBlockingTest {
        val verifier = RealSignatureVerifier(mockPublicKeyCache, TestCoroutineDispatcher())

        verifier.verify(
            KEY_ID,
            "wrong message".byteInputStream(),
            SIGNATURE
        ) shouldEqualTo false
    }

    @Test
    fun `should not verify the signature cache returns null`() = runBlockingTest {
        When calling mockPublicKeyCache[KEY_ID] itReturns null
        val verifier = RealSignatureVerifier(mockPublicKeyCache, TestCoroutineDispatcher())

        verifier.verify(
            KEY_ID,
            "wrong message".byteInputStream(),
            SIGNATURE
        ) shouldEqualTo false
    }
}
