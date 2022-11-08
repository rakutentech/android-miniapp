package com.rakuten.tech.mobile.miniapp.storage.verifier

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
}
