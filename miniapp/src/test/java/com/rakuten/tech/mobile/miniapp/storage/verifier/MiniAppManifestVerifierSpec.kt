package com.rakuten.tech.mobile.miniapp.storage.verifier

import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.mock
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.io.File

@ExperimentalCoroutinesApi
class MiniAppManifestVerifierSpec {
    private val storeHashVerifier: StoreHashVerifier = mock()
    private val file: File = mock()

    @Test
    fun `should verify from StoreHashVerifier`() {
        val verifier: MiniAppManifestVerifier = mock()
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
