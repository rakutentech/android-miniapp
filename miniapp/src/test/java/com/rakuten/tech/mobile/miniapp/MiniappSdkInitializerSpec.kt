package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class MiniappSdkInitializerSpec {
    private val miniappSdkInitializer = MiniappSdkInitializer()
    private lateinit var context: Context

    @Before
    fun setup() {
        context = getApplicationContext()
    }

    @Test
    fun `The MiniappSdkInitializer should be overriden from ContentProvider`() {
        miniappSdkInitializer.onCreate()
        val uri: Uri = mock()
        miniappSdkInitializer.query(
            uri, null, null, null, null
        ) shouldBe null
        miniappSdkInitializer.update(
            uri, null, null, null
        ) shouldBe 0
        miniappSdkInitializer.insert(uri, null) shouldBe null
        miniappSdkInitializer.delete(
            uri, null, null
        ) shouldBe 0
        miniappSdkInitializer.getType(uri) shouldBe null
    }

    @Test
    fun `createMiniAppSdkConfig should return the correct MiniAppSdkConfig`() {
        val testManifestConfig: AppManifestConfig = mock()
        When calling testManifestConfig.rasProjectId() itReturns TEST_HA_ID_PROJECT
        When calling testManifestConfig.baseUrl() itReturns TEST_BASE_URL
        When calling testManifestConfig.isPreviewMode() itReturns false
        When calling testManifestConfig.requireSignatureVerification() itReturns false
        When calling testManifestConfig.hostAppUserAgentInfo() itReturns ""
        When calling testManifestConfig.subscriptionKey() itReturns TEST_HA_SUBSCRIPTION_KEY
        When calling testManifestConfig.maxStorageSizeLimitInBytes() itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES

        val miniAppSdkConfig = miniappSdkInitializer.createMiniAppSdkConfig(testManifestConfig)
        miniAppSdkConfig.rasProjectId shouldBeEqualTo TEST_HA_ID_PROJECT
        miniAppSdkConfig.baseUrl shouldBeEqualTo TEST_BASE_URL
        miniAppSdkConfig.subscriptionKey shouldBeEqualTo TEST_HA_SUBSCRIPTION_KEY
        miniAppSdkConfig.maxStorageSizeLimitInBytes shouldBeEqualTo TEST_MAX_STORAGE_SIZE_IN_BYTES
    }
}
