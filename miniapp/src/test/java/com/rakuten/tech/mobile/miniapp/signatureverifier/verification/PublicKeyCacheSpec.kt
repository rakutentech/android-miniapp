package com.rakuten.tech.mobile.miniapp.signatureverifier.verification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.miniapp.RobolectricBaseSpec
import com.rakuten.tech.mobile.miniapp.signatureverifier.SignatureVerifier
import com.rakuten.tech.mobile.miniapp.signatureverifier.api.PublicKeyFetcher
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import java.io.IOException

class PublicKeyCacheSpec : RobolectricBaseSpec() {

    private val mockFetcher = Mockito.mock(PublicKeyFetcher::class.java)
    private val mockEncryptor = Mockito.mock(AesEncryptor::class.java)
    private val mockMap = HashMap<String, String>()

    @Before
    fun setup() {
        SignatureVerifier.callback = null
        mockMap["test_key_id"] = "encrypted"
        When calling mockFetcher.fetch("test_key_id") itReturns "test_public_key"
        When calling mockEncryptor.encrypt(any(String::class), any()) itReturns "encrypted"
        When calling mockEncryptor.decrypt(eq("encrypted"), any()) itReturns "test_public_key"
    }

    @Test
    fun `should return cached key`() {
        val cache = createCache(map = mockMap)
        cache["test_key_id"] shouldBe ("test_public_key")
        Mockito.verify(mockFetcher, never()).fetch(eq("test_key_id"))
    }

    @Test
    fun `should call fetched when key is removed cached key`() {
        val cache = createCache(map = mockMap)
        cache["test_key_id"] shouldBe ("test_public_key")
        Mockito.verify(mockFetcher, never()).fetch(eq("test_key_id"))

        cache.remove("test_key_id")
        cache["test_key_id"] shouldBe ("test_public_key")
        Mockito.verify(mockFetcher).fetch(eq("test_key_id"))
    }

    @Test
    fun `should call fetcher for key id that is not cached`() {
        val cache = createCache()

        cache["test_key_id"]?.shouldBeEqualTo("test_public_key")
        Mockito.verify(mockFetcher).fetch(eq("test_key_id"))
    }

    @Test
    fun `should return null when key fetcher failed`() {
        val function: (ex: Exception) -> Unit = {}
        val mockCallback = Mockito.mock(function.javaClass)
        SignatureVerifier.callback = mockCallback
        val cache = createCache()
        When calling mockEncryptor.encrypt(any(), any()) itReturns null
        When calling mockFetcher.fetch(eq("test_key_id")) itThrows IOException("test")

        cache["test_key_id"].shouldBeNull()
        Mockito.verify(mockFetcher).fetch(eq("test_key_id"))
        Mockito.verify(mockCallback).invoke(any(IOException::class))
    }

    @Test
    fun `should cache the public key between App launches`() {
        val cache = createCache()

        // fetched and cached
        cache["test_key_id"] shouldBe ("test_public_key")
        Mockito.verify(mockFetcher).fetch(eq("test_key_id"))

        val secondCache = createCache()

        secondCache["test_key_id"] shouldBe ("test_public_key")
        Mockito.verify(mockFetcher).fetch(eq("test_key_id"))
    }

    @Test
    fun `should return cached key when using real encryptor`() {
        TestKeyStore.setup
        val cache = createCache(encryptor = null)

        cache["test_key_id"] shouldBe ("test_public_key")
        Mockito.verify(mockFetcher).fetch(eq("test_key_id"))
    }

    @Test
    fun `should return valid when used actual encryptor`() {
        TestKeyStore.setup
        val cache = createCache(encryptor = AesEncryptor())

        cache["test_key_id"] shouldBe ("test_public_key")
        Mockito.verify(mockFetcher).fetch(eq("test_key_id"))

        cache["test_key_id"] shouldBe ("test_public_key")
        // called again since encryption and decryption (keystore test) will fail and will fetch
        Mockito.verify(mockFetcher, times(2)).fetch(eq("test_key_id"))
    }

    private fun createCache(
        fetcher: PublicKeyFetcher = mockFetcher,
        context: Context = ApplicationProvider.getApplicationContext(),
        encryptor: AesEncryptor? = mockEncryptor,
        map: MutableMap<String, String>? = null
    ) = PublicKeyCache(fetcher, context, "test", encryptor, map)
}
