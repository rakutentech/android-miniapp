package com.rakuten.tech.mobile.miniapp.signatureverifier.verification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.miniapp.RobolectricBaseSpec
import com.rakuten.tech.mobile.miniapp.signatureverifier.PublicKeyFetcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times

@ExperimentalCoroutinesApi
class PublicKeyCacheSpec : RobolectricBaseSpec() {

    private val mockFetcher = Mockito.mock(PublicKeyFetcher::class.java)
    private val mockEncryptor = Mockito.mock(AesEncryptor::class.java)
    private val mockMap = HashMap<String, String>()

    @Before
    fun setup() {
        mockMap["test_key_id"] = "encrypted"
        runBlockingTest {
            When calling mockFetcher.fetch("test_key_id") itReturns "test_public_key"
        }
        When calling mockEncryptor.encrypt(any(String::class), any()) itReturns "encrypted"
        When calling mockEncryptor.decrypt(eq("encrypted"), any()) itReturns "test_public_key"
    }

    @Test
    fun `should return cached key`() {
        runBlockingTest {
            val cache = createCache()
            cache.getKey("test_key_id") shouldBe ("test_public_key")
            Mockito.verify(mockFetcher, never()).fetch(eq("test_key_id"))
        }
    }

    @Test
    fun `should call fetched when key is removed cached key`() {
        runBlockingTest {
            val cache = createCache()
            cache.getKey("test_key_id") shouldBe ("test_public_key")
            Mockito.verify(mockFetcher, never()).fetch(eq("test_key_id"))

            cache.remove("test_key_id")
            cache.getKey("test_key_id") shouldBe ("test_public_key")
            Mockito.verify(mockFetcher).fetch(eq("test_key_id"))
        }
    }

    @Test
    fun `should call fetcher for key id that is not cached`() {
        runBlockingTest {
            val cache = createCache()
            cache.getKey("test_key_id")?.shouldBeEqualTo("test_public_key")
            Mockito.verify(mockFetcher).fetch(eq("test_key_id"))
        }
    }

    @Test
    fun `should cache the public key between App launches`() {
        runBlockingTest {
            val cache = createCache()

            // fetched and cached
            cache.getKey("test_key_id") shouldBe ("test_public_key")
            Mockito.verify(mockFetcher).fetch(eq("test_key_id"))

            val secondCache = createCache()

            secondCache.getKey("test_key_id") shouldBe ("test_public_key")
            Mockito.verify(mockFetcher).fetch(eq("test_key_id"))
        }
    }

    @Test
    fun `should return cached key when using real encryptor`() {
        runBlockingTest {
            TestKeyStore.setup
            val cache = createCache()

            cache.getKey("test_key_id") shouldBe ("test_public_key")
            Mockito.verify(mockFetcher).fetch(eq("test_key_id"))
        }
    }

    @Test
    fun `should return valid when used actual encryptor`() {
        runBlockingTest {
            TestKeyStore.setup
            val cache = createCache()

            cache.getKey("test_key_id") shouldBe ("test_public_key")
            Mockito.verify(mockFetcher).fetch(eq("test_key_id"))

            cache.getKey("test_key_id") shouldBe ("test_public_key")
            // called again since encryption and decryption (keystore test) will fail and will fetch
            Mockito.verify(mockFetcher, times(2)).fetch(eq("test_key_id"))
        }
    }

    private fun createCache(
        fetcher: PublicKeyFetcher = mockFetcher,
        context: Context = ApplicationProvider.getApplicationContext(),
    ) = PublicKeyCache(fetcher, context, "test")
}
