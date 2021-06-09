package com.rakuten.tech.mobile.miniapp.storage.verifier

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.MiniAppVerificationException
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class StoreHashVerifierSpec {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefs = context.getSharedPreferences("test-cache", Context.MODE_PRIVATE)
    private val dispatcher = TestCoroutineDispatcher()
    private val verifier = StoreHashVerifier(prefs, dispatcher)

    @Test
    fun `should verify hash for data`() = runBlockingTest {
        val hash = "hash-data"
        verifier.storeHashAsync(TEST_MA_ID, hash)
        verifier.verify(TEST_MA_ID, hash) shouldBe true
    }

    @Test
    fun `should fail to verify hash when data has been modified`() = runBlockingTest {
        var hash = "hash-data"
        verifier.storeHashAsync(TEST_MA_ID, hash)
        hash = "another-hash-data"
        verifier.verify(TEST_MA_ID, hash) shouldBe false
    }

    @Test(expected = MiniAppVerificationException::class)
    fun `should throw exception when there is problem with device keystore`() {
        // cannot retrieve key store from test context.
        StoreHashVerifier(context, "")
    }
}
