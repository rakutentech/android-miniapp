package com.rakuten.tech.mobile.miniapp.signatureverifier

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.miniapp.RobolectricBaseSpec
import org.amshove.kluent.any
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class SignatureVerifierSpec : RobolectricBaseSpec() {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val function: (ex: Exception) -> Unit = {}
    private val mockCb = Mockito.mock(function.javaClass)

    @Test
    fun `should initialize instance with correct parameters`() {
        val instance = SignatureVerifier.init(context, BASE_URL, SUB_KEY)

        instance.shouldNotBeNull()
        instance shouldBeInstanceOf RealSignatureVerifier::class
    }

    @Test
    fun `should initialize instance with correct parameters with callback`() {
        val instance = SignatureVerifier.init(context, BASE_URL, SUB_KEY) {
            Assert.fail()
        }

        instance.shouldNotBeNull()
        instance shouldBeInstanceOf RealSignatureVerifier::class
    }

    @Test
    fun `should return null initialization failed due to context`() {
        val mockContext = Mockito.mock(Context::class.java)
        SignatureVerifier.init(mockContext, BASE_URL, SUB_KEY).shouldBeNull()
    }

    @Test
    fun `should return null initialization failed due to context with callback`() {
        val mockContext = Mockito.mock(Context::class.java)
        SignatureVerifier.init(mockContext, BASE_URL, SUB_KEY, mockCb).shouldBeNull()

        Mockito.verify(mockCb).invoke(any())
    }

    @Test
    fun `should return null initialization failed due to invalid endpoint`() {
        SignatureVerifier.init(context, "", SUB_KEY).shouldBeNull()
    }

    @Test
    fun `should return null initialization failed due to invalid endpoint with callback`() {
        SignatureVerifier.init(context, "", SUB_KEY, mockCb).shouldBeNull()

        Mockito.verify(mockCb).invoke(any(SignatureVerifierException::class))
    }

    @Test
    fun `should return null initialization failed due to invalid key`() {
        SignatureVerifier.init(context, BASE_URL, "").shouldBeNull()
    }

    @Test
    fun `should return null initialization failed due to invalid key with callback`() {
        SignatureVerifier.init(context, BASE_URL, "", mockCb).shouldBeNull()

        Mockito.verify(mockCb).invoke(any(SignatureVerifierException::class))
    }

    companion object {
        private const val BASE_URL = "http://sample.com"
        private const val SUB_KEY = "test_key"
    }
}
