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

@Suppress("LargeClass")
@RunWith(AndroidJUnit4::class)
class MiniappSdkInitializerSpec {
    private val miniappSdkInitializer = MiniappSdkInitializer()
    private lateinit var context: Context

    @Before
    fun setup() {
        context = getApplicationContext()
    }

    @Test
    fun `query should be null when calls overrode query`() {
        miniappSdkInitializer.onCreate()
        val uri: Uri = mock()
        miniappSdkInitializer.query(
            uri, null, null, null, null
        ) shouldBe null
    }

    @Test
    fun `update should be null when calls overrode update`() {
        miniappSdkInitializer.onCreate()
        val uri: Uri = mock()
        miniappSdkInitializer.update(
            uri, null, null, null
        ) shouldBe 0
    }

    @Test
    fun `insert should be null when calls overrode insert`() {
        miniappSdkInitializer.onCreate()
        val uri: Uri = mock()
        miniappSdkInitializer.insert(uri, null) shouldBe null
    }

    @Test
    fun `delete should be null when calls overrode delete`() {
        miniappSdkInitializer.onCreate()
        val uri: Uri = mock()
        miniappSdkInitializer.delete(
            uri, null, null
        ) shouldBe 0
    }

    @Test
    fun `getType should be overriden from ContentProvider`() {
        miniappSdkInitializer.onCreate()
        val uri: Uri = mock()
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

    private fun getMockContextAndPackageManager(onGenerated: (Context, PackageManager) -> Unit) {
        val context: Context = mock()
        val packageManager: PackageManager = mock()
        val applicationInfo: ApplicationInfo = mock()

        When calling context.packageManager itReturns packageManager
        When calling context.packageName itReturns TEST_PACKAGE_NAME
        When calling packageManager.getApplicationInfo(
            TEST_PACKAGE_NAME, PackageManager.GET_META_DATA
        ) itReturns applicationInfo

        onGenerated(context, packageManager)
    }

    private fun getAppManifestConfigAndSdkConfig(onGenerated: (AppManifestConfig, MiniappSdkInitializer) -> Unit) {
        val appManifestConfig = getMockAppManifestConfig()
        val sdkInitializer: MiniappSdkInitializer = spy(MiniappSdkInitializer())

        When calling sdkInitializer.context itReturns context
        When calling sdkInitializer.createAppManifestConfig(context) itReturns appManifestConfig

        onGenerated(appManifestConfig, sdkInitializer)
    }

    @Test
    fun `createMiniAppSdkConfig should return the correct MiniAppSdkConfig`() {
        val testManifestConfig: AppManifestConfig = getMockAppManifestConfig()

        val miniAppSdkConfig = miniappSdkInitializer.createMiniAppSdkConfig(testManifestConfig)
        miniAppSdkConfig.rasProjectId shouldBeEqualTo TEST_HA_ID_PROJECT
        miniAppSdkConfig.baseUrl shouldBeEqualTo TEST_BASE_URL
        miniAppSdkConfig.subscriptionKey shouldBeEqualTo TEST_HA_SUBSCRIPTION_KEY
        miniAppSdkConfig.maxStorageSizeLimitInBytes shouldBeEqualTo TEST_MAX_STORAGE_SIZE_IN_BYTES
    }

    @Test
    fun `calling applicationInfo inside createAppManifestConfig should not fail`() {
        getMockContextAndPackageManager { context, packageManager ->
            miniappSdkInitializer.createAppManifestConfig(context)
            verify(packageManager).getApplicationInfo(any(), any())
        }
    }

    @Test
    fun `calling packageName inside createAppManifestConfig should not fail`() {
        getMockContextAndPackageManager { context, _ ->
            miniappSdkInitializer.createAppManifestConfig(context)
            verify(context).packageName
        }
    }

    @Test
    fun `calling createAppManifestConfig should generate AppManifestConfig`() {
        getMockContextAndPackageManager { context, _ ->
            val appManifestConfig = miniappSdkInitializer.createAppManifestConfig(context)
            appManifestConfig.shouldBeInstanceOf<AppManifestConfig>()
        }
    }

    @Test
    fun `calling createAppManifestConfig should not be null`() {
        getMockContextAndPackageManager { context, _ ->
            val appManifestConfig = miniappSdkInitializer.createAppManifestConfig(context)
            appManifestConfig.shouldNotBeNull()
        }
    }

    @Suppress("MaxLineLength")
    @Test
    fun `calling packageManager inside  should not fail`() {
        getMockContextAndPackageManager { context, _ ->
            miniappSdkInitializer.createAppManifestConfig(context)
            verify(context).packageManager
        }
    }

    @Test
    fun `onCreate should call createAppManifestConfig`() {
        getAppManifestConfigAndSdkConfig { _, miniappSdkInitializer ->
            miniappSdkInitializer.onCreate()
            verify(miniappSdkInitializer).createAppManifestConfig(context)
        }
    }

    @Test
    fun `onCreate should call createMiniAppSdkConfig`() {
        getAppManifestConfigAndSdkConfig { appManifestConfig, miniappSdkInitializer ->
            miniappSdkInitializer.onCreate()
            verify(miniappSdkInitializer).createMiniAppSdkConfig(appManifestConfig)
        }
    }

    @Test
    fun `onCreate should call executeMiniAppAnalytics`() {
        getAppManifestConfigAndSdkConfig { appManifestConfig, miniappSdkInitializer ->
            miniappSdkInitializer.onCreate()
            verify(miniappSdkInitializer).executeMiniAppAnalytics(appManifestConfig.rasProjectId())
        }
    }

    @Test
    fun `MiniApp should be called when calls onCreate()`() {
        getAppManifestConfigAndSdkConfig { _, miniappSdkInitializer ->
            val isSdkInitialized = miniappSdkInitializer.onCreate()
            isSdkInitialized.shouldBe(true)
        }
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
