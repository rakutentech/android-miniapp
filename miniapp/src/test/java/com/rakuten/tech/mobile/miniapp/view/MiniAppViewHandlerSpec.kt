package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.File
import java.util.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Suppress("LargeClass")
open class MiniAppViewHandlerSpec {
    private lateinit var miniAppViewHandler: MiniAppViewHandler
    private val context: Context = ApplicationProvider.getApplicationContext()
    private var miniAppSdkConfig: MiniAppSdkConfig = mock()
    private var apiClient: ApiClient = mock()
    val dummyManifest: MiniAppManifest = MiniAppManifest(
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
        listOf(),
        TEST_ATP_LIST,
        mapOf(),
        TEST_MA_VERSION_ID
    )
    private val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
    private val deniedPermission = MiniAppCustomPermission(
        TEST_MA_ID,
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
    )
    private val file: File = mock()
    private val languageCode = Locale.forLanguageTag(context.getString(R.string.miniapp_sdk_android_locale)).language
    val miniAppConfig: MiniAppConfig = mock()
    val miniAppMessageBridge: MiniAppMessageBridge = mock()

    @Before
    fun setup() {
        FakeAndroidKeyStore.setup

        When calling miniAppSdkConfig.baseUrl itReturns TEST_BASE_URL
        When calling miniAppSdkConfig.rasProjectId itReturns TEST_HA_ID_PROJECT
        When calling miniAppSdkConfig.subscriptionKey itReturns TEST_HA_SUBSCRIPTION_KEY
        When calling miniAppSdkConfig.miniAppAnalyticsConfigList itReturns TEST_HA_ANALYTICS_CONFIGS
        When calling miniAppSdkConfig.hostAppUserAgentInfo itReturns ""
        When calling miniAppSdkConfig.maxStorageSizeLimitInBytes itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES

        miniAppViewHandler = spy(MiniAppViewHandler(context, miniAppSdkConfig))

        miniAppViewHandler.apiClientRepository = mock()
        When calling miniAppViewHandler.apiClientRepository.getApiClientFor(miniAppSdkConfig) itReturns apiClient

        miniAppViewHandler.downloadedManifestCache = mock()
        miniAppViewHandler.miniAppManifestVerifier = mock()
        miniAppViewHandler.miniAppCustomPermissionCache = mock()
        miniAppViewHandler.downloadedManifestCache = mock()
        miniAppViewHandler.miniAppDownloader = mock()
        miniAppViewHandler.displayer = mock()

        When calling miniAppViewHandler
            .downloadedManifestCache.readDownloadedManifest(TEST_MA_ID) itReturns cachedManifest
        When calling miniAppViewHandler
            .downloadedManifestCache.getManifestFile(TEST_MA_ID) itReturns file
        When calling miniAppViewHandler.miniAppManifestVerifier.verify(
            TEST_MA_ID,
            file
        ) itReturns true
    }

    /** region: Manifest verification */
    @Test(expected = RequiredPermissionsNotGrantedException::class)
    fun `verifyManifest will throw exception when required permissions are denied`() =
        runBlockingTest {
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID,
                languageCode
            ) itReturns dummyManifest
            When calling miniAppViewHandler
                .miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling miniAppViewHandler.downloadedManifestCache.isRequiredPermissionDenied(
                deniedPermission
            ) itReturns true

            miniAppViewHandler.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
        }

    @Test(expected = RequiredPermissionsNotGrantedException::class)
    fun `verifyManifest will throw exception when required permissions are denied when fromCache true`() =
        runBlockingTest {
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID,
                languageCode
            ) itReturns dummyManifest
            When calling miniAppViewHandler
                .miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling miniAppViewHandler.downloadedManifestCache.isRequiredPermissionDenied(
                deniedPermission
            ) itReturns true

            miniAppViewHandler.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID, true)
        }

    @Test
    fun `verifyManifest will execute successfully when required permissions are accepted`() =
        runBlockingTest {
            val allowedPermission = MiniAppCustomPermission(
                TEST_MA_ID,
                listOf(
                    Pair(
                        MiniAppCustomPermissionType.USER_NAME,
                        MiniAppCustomPermissionResult.ALLOWED
                    )
                )
            )

            val manifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
            When calling miniAppViewHandler.downloadedManifestCache.readDownloadedManifest(
                TEST_MA_ID
            ) itReturns manifest
            When calling miniAppViewHandler
                .miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns allowedPermission
            When calling miniAppViewHandler.downloadedManifestCache.getAllPermissions(
                allowedPermission
            ) itReturns
                    allowedPermission.pairValues
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID,
                languageCode
            ) itReturns dummyManifest

            miniAppViewHandler.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)

            verify(miniAppViewHandler.downloadedManifestCache, times(0)).storeDownloadedManifest(
                TEST_MA_ID, cachedManifest
            )
            verify(miniAppViewHandler).isManifestEqual(dummyManifest, dummyManifest)
            verify(miniAppViewHandler.miniAppCustomPermissionCache).removePermissionsNotMatching(
                TEST_MA_ID, allowedPermission.pairValues
            )
        }

    @Test
    fun `verifyManifest will store api manifest when version ids are different`() =
        runBlockingTest {
            val differentVersionId = "another_version_id"
            val manifestToStore = CachedManifest(differentVersionId, dummyManifest)
            val manifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID,
                differentVersionId,
                languageCode
            ) itReturns dummyManifest
            When calling miniAppViewHandler
                .miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling miniAppViewHandler.downloadedManifestCache.getAllPermissions(
                deniedPermission
            ) itReturns
                    deniedPermission.pairValues

            miniAppViewHandler.verifyManifest(TEST_MA_ID, differentVersionId)

            verify(miniAppViewHandler).checkToDownloadManifest(
                TEST_MA_ID,
                differentVersionId,
                manifest
            )
            verify(miniAppViewHandler.downloadedManifestCache).storeDownloadedManifest(
                TEST_MA_ID,
                manifestToStore
            )
            verify(miniAppViewHandler.miniAppCustomPermissionCache).removePermissionsNotMatching(
                TEST_MA_ID,
                deniedPermission.pairValues
            )
        }

    @Test
    fun `verifyManifest will download api manifest when hash has not been verified`() =
        runBlockingTest {
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID,
                "en"
            ) itReturns dummyManifest
            When calling miniAppViewHandler
                .miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling miniAppViewHandler.miniAppManifestVerifier.verify(
                TEST_MA_ID,
                file
            ) itReturns false

            miniAppViewHandler.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)

            verify(miniAppViewHandler, times(2)).checkToDownloadManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID,
                cachedManifest
            )
        }

    @Test
    fun `checkToDownloadManifest will store manifest and hash properly`() =
        runBlockingTest {
            val manifest = MiniAppManifest(
                listOf(
                    Pair(MiniAppCustomPermissionType.USER_NAME, "reason"),
                    Pair(MiniAppCustomPermissionType.CONTACT_LIST, "reason")
                ), listOf(),
                TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
            )
            val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, manifest)
            val manifestToStore = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID,
                languageCode
            ) itReturns dummyManifest
            miniAppViewHandler.checkToDownloadManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID,
                cachedManifest
            )

            verify(miniAppViewHandler.downloadedManifestCache).storeDownloadedManifest(
                TEST_MA_ID,
                manifestToStore
            )
            verify(miniAppViewHandler.miniAppManifestVerifier).storeHashAsync(TEST_MA_ID, file)
        }

    @Test
    fun `isManifestEqual will return true when both api and downloaded manifest are equal`() {
        miniAppViewHandler.isManifestEqual(dummyManifest, dummyManifest) shouldBeEqualTo true
    }

    @Test
    fun `isManifestEqual will return false when api and downloaded manifest are not equal`() {
        val dummyApiManifest = MiniAppManifest(
            listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, "reason"),
                Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "reason")
            ),
            listOf(),
            TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
        )
        miniAppViewHandler.isManifestEqual(dummyApiManifest, dummyManifest) shouldBeEqualTo false
    }

    @Test
    fun `isManifestEqual will return false when api and downloaded manifest are null`() {
        miniAppViewHandler.isManifestEqual(null, null) shouldBeEqualTo false
    }

    /** end region */

    @Test
    fun `api manifest should be fetched from MiniAppDownloader`() =
        runBlockingTest {
            miniAppViewHandler.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID, languageCode)
            verify(miniAppViewHandler.miniAppDownloader).fetchMiniAppManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID,
                languageCode
            )
        }

    @Test
    fun `api manifest should not be fetched from MiniAppDownloader when different languageCode`() =
        runBlockingTest {
            miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID,
                languageCode
            )
            verify(miniAppViewHandler.miniAppDownloader, times(0))
                .fetchMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID, "")
        }

    /** region: RealMiniApp.create */
    private fun onGettingManifestWhileCreate() = runBlockingTest {
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
        When calling miniAppViewHandler
            .downloadedManifestCache.readDownloadedManifest(TEST_MA_ID) itReturns cachedManifest
        When calling miniAppViewHandler.getMiniAppManifest(
            TEST_MA_ID, TEST_MA_VERSION_ID, languageCode
        ) itReturns dummyManifest
    }

    @Test
    @Suppress("LongMethod")
    fun `should invoke MiniAppDownloader, Displayer and verifyManifest while mini app view creation by miniAppId`() =
        runBlockingTest {
            onGettingManifestWhileCreate()
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppViewHandler.miniAppDownloader.getMiniApp(TEST_MA_ID) itReturns getMiniAppResult
            When calling miniAppConfig.miniAppMessageBridge itReturns miniAppMessageBridge
            When calling miniAppConfig.queryParams itReturns ""

            miniAppViewHandler.createMiniAppView(TEST_MA_ID, miniAppConfig)

            verify(miniAppViewHandler.miniAppDownloader).getMiniApp(TEST_MA_ID)
            verify(miniAppViewHandler).verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
            verify(miniAppViewHandler.displayer).createMiniAppDisplay(
                getMiniAppResult.first,
                getMiniAppResult.second,
                miniAppConfig.miniAppMessageBridge,
                null,
                null,
                miniAppViewHandler.miniAppCustomPermissionCache,
                miniAppViewHandler.downloadedManifestCache,
                "",
                miniAppViewHandler.miniAppAnalytics,
                miniAppViewHandler.ratDispatcher,
                miniAppViewHandler.secureStorageDispatcher,
                false
            )
        }

    @Test
    @Suppress("LongMethod")
    fun `should invoke MiniAppDownloader, Displayer and verifyManifest while mini app view creation by miniAppInfo`() =
        runBlockingTest {
            onGettingManifestWhileCreate()
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            val testVersion = Version("", TEST_MA_VERSION_ID)
            val testMiniAppInfo = TEST_MA.copy(id = TEST_MA_ID, version = testVersion)

            When calling miniAppViewHandler.miniAppDownloader.getMiniApp(testMiniAppInfo) itReturns getMiniAppResult
            When calling miniAppConfig.miniAppMessageBridge itReturns miniAppMessageBridge
            When calling miniAppConfig.queryParams itReturns ""

            miniAppViewHandler.createMiniAppView(testMiniAppInfo, miniAppConfig)

            verify(miniAppViewHandler.miniAppDownloader).getMiniApp(testMiniAppInfo)
            verify(miniAppViewHandler).verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
            verify(miniAppViewHandler.displayer).createMiniAppDisplay(
                getMiniAppResult.first,
                getMiniAppResult.second,
                miniAppConfig.miniAppMessageBridge,
                null,
                null,
                miniAppViewHandler.miniAppCustomPermissionCache,
                miniAppViewHandler.downloadedManifestCache,
                "",
                miniAppViewHandler.miniAppAnalytics,
                miniAppViewHandler.ratDispatcher,
                miniAppViewHandler.secureStorageDispatcher,
                false
            )
        }

    @Test
    fun `should invoke getCachedMiniApp while mini app view creation if fromCache is true`() =
        runBlockingTest {
            onGettingManifestWhileCreate()
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppViewHandler.miniAppDownloader.getCachedMiniApp(TEST_MA_ID) itReturns getMiniAppResult

            miniAppViewHandler.createMiniAppView(TEST_MA_ID, miniAppConfig, true)
            verify(miniAppViewHandler.miniAppDownloader).getCachedMiniApp(TEST_MA_ID)
        }

    @Test
    @Suppress("LongMethod")
    fun `should invoke getMiniApp while mini app view creation if fromCache is false`() =
        runBlockingTest {
            onGettingManifestWhileCreate()
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppViewHandler.miniAppDownloader.getMiniApp(TEST_MA_ID) itReturns getMiniAppResult

            miniAppViewHandler.createMiniAppView(TEST_MA_ID, miniAppConfig, false)
            verify(miniAppViewHandler.miniAppDownloader).getMiniApp(TEST_MA_ID)
        }

    @Test
    fun `should invoke validateHttpAppUrl while miniapp creation with valid url`() =
        runBlockingTest {
            When calling miniAppConfig.miniAppMessageBridge itReturns miniAppMessageBridge
            When calling miniAppConfig.queryParams itReturns ""

            miniAppViewHandler.createMiniAppViewWithUrl(TEST_MA_URL, miniAppConfig)

            verify(miniAppViewHandler.miniAppDownloader).validateHttpAppUrl(TEST_MA_URL)
            verify(miniAppViewHandler.displayer).createMiniAppDisplay(
                TEST_MA_URL,
                miniAppMessageBridge,
                null,
                null,
                miniAppViewHandler.miniAppCustomPermissionCache,
                miniAppViewHandler.downloadedManifestCache,
                "",
                miniAppViewHandler.miniAppAnalytics,
                miniAppViewHandler.ratDispatcher,
                miniAppViewHandler.secureStorageDispatcher,
                false
            )
        }
    /** end region */
}
