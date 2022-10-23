package com.rakuten.tech.mobile.miniapp.storage.verifier

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.MiniAppVerificationException
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CachedMiniAppVerifierSpec {
    private val storeHashVerifier: StoreHashVerifier = mock()
    private val file: File = mock()

    private fun withRealActivity(onReady: (CachedMiniAppVerifier) -> Unit) {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val cachedMiniAppVerifier = spy(CachedMiniAppVerifier(activity))
            onReady(cachedMiniAppVerifier)
        }
    }

    @Test
    fun `should verify from StoreHashVerifier`() {
        val verifier: CachedMiniAppVerifier = mock()
        doReturn(storeHashVerifier).whenever(verifier).storeHashVerifier
        verifier.verify(TEST_MA_ID, file)
        verifier.storeHashVerifier.verify(TEST_MA_ID, file)
    }

    @Test
    fun `should store a hash using StoreHashVerifier`() = runBlockingTest {
        val verifier: CachedMiniAppVerifier = mock()
        doReturn(storeHashVerifier).whenever(verifier).storeHashVerifier
        verifier.storeHashAsync(TEST_MA_ID, file)
        verifier.storeHashVerifier.storeHashAsync(TEST_MA_ID, file)
    }

    @Test(expected = MiniAppVerificationException::class)
    fun `verify should call storeHashVerifier verify`() {
        withRealActivity { cachedMiniAppVerifier ->
            cachedMiniAppVerifier.verify(TEST_MA_ID, mock())
            verify(cachedMiniAppVerifier).storeHashVerifier.verify(eq(TEST_MA_ID), any())
        }
    }

    @Test(expected = MiniAppVerificationException::class)
    fun `storeHashSync should call storeHashVerifier storeHashAsync`() = runBlockingTest {
        withRealActivity { cachedMiniAppVerifier ->
            launch {
                cachedMiniAppVerifier.storeHashAsync(TEST_MA_ID, mock())
                verify(storeHashVerifier).storeHashAsync(eq(TEST_MA_ID), any())
            }
        }
    }
}
