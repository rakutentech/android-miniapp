package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.MiniAppVerificationException
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class MiniAppManifestVerifierSpec {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefs = context.getSharedPreferences("test-cache", Context.MODE_PRIVATE)
    private val dispatcher = TestCoroutineDispatcher()
    private val verifier = MiniAppManifestVerifier(prefs, dispatcher)

    @Test
    fun `should verify hash for manifest`() = runBlockingTest {
        val manifest = MiniAppManifest(listOf(), listOf(), listOf(), mapOf(), TEST_MA_VERSION_ID)
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, manifest)
        verifier.storeHashAsync(TEST_MA_ID, cachedManifest)
        verifier.verify(TEST_MA_ID, cachedManifest) shouldBe true
    }

    @Test
    fun `should fail to verify hash when manifest has been modified`() = runBlockingTest {
        val manifest = MiniAppManifest(listOf(), listOf(), listOf(), mapOf(), TEST_MA_VERSION_ID)
        var cachedManifest = CachedManifest(TEST_MA_VERSION_ID, manifest)
        verifier.storeHashAsync(TEST_MA_ID, cachedManifest)
        cachedManifest = CachedManifest("another_version_id", manifest)
        verifier.verify(TEST_MA_ID, cachedManifest) shouldBe false
    }

    @Test(expected = MiniAppVerificationException::class)
    fun `should throw exception when there is problem with device keystore`() {
        // cannot retrieve key store from test context.
        MiniAppManifestVerifier(context)
    }
}
