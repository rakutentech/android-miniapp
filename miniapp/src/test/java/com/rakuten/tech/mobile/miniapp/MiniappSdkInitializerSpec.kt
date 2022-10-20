package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

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

    private fun getMockAppManifestConfig(): AppManifestConfig {
        val testManifestConfig: AppManifestConfig = mock()
        When calling testManifestConfig.rasProjectId() itReturns TEST_HA_ID_PROJECT
        When calling testManifestConfig.baseUrl() itReturns TEST_BASE_URL
        When calling testManifestConfig.isPreviewMode() itReturns false
        When calling testManifestConfig.requireSignatureVerification() itReturns false
        When calling testManifestConfig.hostAppUserAgentInfo() itReturns ""
        When calling testManifestConfig.subscriptionKey() itReturns TEST_HA_SUBSCRIPTION_KEY
        When calling testManifestConfig.maxStorageSizeLimitInBytes() itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES
        return testManifestConfig
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

    @Suppress("MaxLineLength")
    @Test
    fun `calling createAppManifestConfig should generate AppManifestConfig`() {
        val context: Context = mock()
        val packageManager: PackageManager = mock()
        val applicationInfo: ApplicationInfo = mock()

        When calling context.packageManager itReturns packageManager
        When calling context.packageName itReturns "com.rakuten.tech.mobile.miniapp"
        When calling packageManager.getApplicationInfo(
            "com.rakuten.tech.mobile.miniapp", PackageManager.GET_META_DATA
        ) itReturns applicationInfo

        val appManifestConfig = miniappSdkInitializer.createAppManifestConfig(context)

        verify(context).packageManager
        verify(packageManager).getApplicationInfo(any(), any())
        context.packageName.shouldBeEqualTo("com.rakuten.tech.mobile.miniapp")
        appManifestConfig.shouldNotBeNull()
        appManifestConfig.shouldBeInstanceOf<AppManifestConfig>()
    }

    @Test
    fun `MiniApp should be called when calls onCreate()`() {
        val appManifestConfig = getMockAppManifestConfig()
        val sdkInitializer: MiniappSdkInitializer = spy(MiniappSdkInitializer())

        When calling sdkInitializer.context itReturns context
        When calling sdkInitializer.createAppManifestConfig(context) itReturns appManifestConfig

        val isSdkInitializerLoaded = sdkInitializer.onCreate()
        verify(sdkInitializer).createAppManifestConfig(context)
        verify(sdkInitializer).createMiniAppSdkConfig(appManifestConfig)
        verify(sdkInitializer).executeMiniAppAnalytics(appManifestConfig.rasProjectId())
        isSdkInitializerLoaded.shouldBe(true)
    }

    @Test
    fun `onCreate() should be false when context is null`() {
        val sdkInitializer: MiniappSdkInitializer = spy(MiniappSdkInitializer())
        val context: Context? = null
        When calling sdkInitializer.context itReturns context
        val isSdkInitializerLoaded = sdkInitializer.onCreate()
        isSdkInitializerLoaded.shouldBe(false)
    }

    @Test
    fun `should trigger MiniAppAnalytics if executeMiniAppAnalytics is called`() {
        val sdkInitializer: MiniappSdkInitializer = spy(MiniappSdkInitializer())
        sdkInitializer.executeMiniAppAnalytics(TEST_HA_ID_PROJECT)
        Mockito.mockStatic(MiniAppAnalytics::class.java).use { utilities ->
            utilities.verifyNoMoreInteractions()
        }
    }
}
