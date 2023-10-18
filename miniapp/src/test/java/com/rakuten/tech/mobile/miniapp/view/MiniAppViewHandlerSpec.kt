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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Suppress("LargeClass", "DeferredResultUnused", "MaxLineLength")
open class MiniAppViewHandlerSpec {
    private lateinit var miniAppViewHandler: MiniAppViewHandler
    private val context: Context = ApplicationProvider.getApplicationContext()
    private var miniAppSdkConfig: MiniAppSdkConfig = MiniAppSdkConfig(
        baseUrl = TEST_BASE_URL,
        rasProjectId = TEST_HA_ID_PROJECT,
        subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
        miniAppAnalyticsConfigList = TEST_HA_ANALYTICS_CONFIGS,
        hostAppUserAgentInfo = "",
        maxStorageSizeLimitInBytes = TEST_MAX_STORAGE_SIZE_IN_BYTES,
        sslPinningPublicKeyList = emptyList(),
        isPreviewMode = true,
        requireSignatureVerification = false,
        hostAppVersionId = ""
    )
    private var apiClient: ApiClient = mock()
    private val dummyManifest: MiniAppManifest = MiniAppManifest(
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
    private val languageCode =
        Locale.forLanguageTag(context.getString(R.string.miniapp_sdk_android_locale)).language
    private val miniAppConfig: MiniAppConfig = mock()
    private val miniAppMessageBridge: MiniAppMessageBridge = mock()

    @Before
    fun setup() {
        FakeAndroidKeyStore.setup

        miniAppViewHandler = spy(MiniAppViewHandler(context, miniAppSdkConfig))
        When calling miniAppViewHandler.apiClientRepository itReturns mock()
        When calling miniAppViewHandler.apiClientRepository.getApiClientFor(miniAppSdkConfig) itReturns apiClient

        When calling miniAppViewHandler.downloadedManifestCache itReturns mock()
        When calling miniAppViewHandler.miniAppManifestVerifier itReturns mock()
        When calling miniAppViewHandler.miniAppCustomPermissionCache itReturns mock()
        When calling miniAppViewHandler.downloadedManifestCache itReturns mock()
        When calling miniAppViewHandler.miniAppDownloader itReturns mock()
        When calling miniAppViewHandler.displayer itReturns mock()
        When calling miniAppViewHandler.miniAppVerifier itReturns mock()
        When calling miniAppViewHandler.miniAppStorage itReturns mock()

        When calling miniAppViewHandler.downloadedManifestCache
            .readDownloadedManifest(TEST_MA_ID) itReturns cachedManifest
        When calling miniAppViewHandler.downloadedManifestCache.getManifestFile(TEST_MA_ID) itReturns file
        When calling miniAppViewHandler.miniAppManifestVerifier.verify(
            TEST_MA_ID, file
        ) itReturns true
    }

    /** region: Manifest verification */
    @Test(expected = RequiredPermissionsNotGrantedException::class)
    fun `verifyManifest will throw exception when required permissions are denied`() =
        runBlockingTest {
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID, TEST_MA_VERSION_ID, languageCode
            ) itReturns dummyManifest
            When calling miniAppViewHandler.miniAppCustomPermissionCache
                .readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling miniAppViewHandler.downloadedManifestCache
                .isRequiredPermissionDenied(deniedPermission) itReturns true

            miniAppViewHandler.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
        }

    @Test(expected = RequiredPermissionsNotGrantedException::class)
    fun `verifyManifest will throw exception when required permissions are denied when fromCache true`() =
        runBlockingTest {
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID, TEST_MA_VERSION_ID, languageCode
            ) itReturns dummyManifest
            When calling miniAppViewHandler.miniAppCustomPermissionCache
                .readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling miniAppViewHandler.downloadedManifestCache.isRequiredPermissionDenied(
                deniedPermission) itReturns true

            miniAppViewHandler.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID, true)
        }

    @Test
    fun `verifyManifest will execute successfully when required permissions are accepted`() =
        runBlockingTest {
            val allowedPermission = MiniAppCustomPermission(
                TEST_MA_ID, listOf(
                    Pair(
                        MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED
                    )
                )
            )

            val manifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
            When calling miniAppViewHandler.downloadedManifestCache.readDownloadedManifest(
                TEST_MA_ID
            ) itReturns manifest
            When calling miniAppViewHandler.miniAppCustomPermissionCache
                .readPermissions(TEST_MA_ID) itReturns allowedPermission
            When calling miniAppViewHandler.downloadedManifestCache.getAllPermissions(
                allowedPermission
            ) itReturns allowedPermission.pairValues
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID, TEST_MA_VERSION_ID, languageCode
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
                TEST_MA_ID, differentVersionId, languageCode
            ) itReturns dummyManifest
            When calling miniAppViewHandler.miniAppCustomPermissionCache
                .readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling miniAppViewHandler.downloadedManifestCache.getAllPermissions(
                deniedPermission) itReturns deniedPermission.pairValues

            miniAppViewHandler.verifyManifest(TEST_MA_ID, differentVersionId)

            verify(miniAppViewHandler).checkToDownloadManifest(
                TEST_MA_ID, differentVersionId, manifest
            )
            verify(miniAppViewHandler.downloadedManifestCache).storeDownloadedManifest(
                TEST_MA_ID, manifestToStore
            )
            verify(miniAppViewHandler.miniAppCustomPermissionCache).removePermissionsNotMatching(
                TEST_MA_ID, deniedPermission.pairValues
            )
        }

    @Test
    fun `verifyManifest will download api manifest when hash has not been verified`() =
        runBlockingTest {
            When calling miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID, TEST_MA_VERSION_ID, "en"
            ) itReturns dummyManifest
            When calling miniAppViewHandler.miniAppCustomPermissionCache
                .readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling miniAppViewHandler.miniAppManifestVerifier.verify(
                TEST_MA_ID, file) itReturns false

            miniAppViewHandler.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)

            verify(miniAppViewHandler, times(2)).checkToDownloadManifest(
                TEST_MA_ID, TEST_MA_VERSION_ID, cachedManifest
            )
        }

    @Test
    fun `checkToDownloadManifest will store manifest and hash properly`() = runBlockingTest {
        val manifest = MiniAppManifest(
            listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, "reason"),
                Pair(MiniAppCustomPermissionType.CONTACT_LIST, "reason")
            ), listOf(), TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
        )
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, manifest)
        val manifestToStore = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
        When calling miniAppViewHandler.getMiniAppManifest(
            TEST_MA_ID, TEST_MA_VERSION_ID, languageCode
        ) itReturns dummyManifest
        miniAppViewHandler.checkToDownloadManifest(
            TEST_MA_ID, TEST_MA_VERSION_ID, cachedManifest
        )

        verify(miniAppViewHandler.downloadedManifestCache).storeDownloadedManifest(
            TEST_MA_ID, manifestToStore
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
            ), listOf(
                Pair(MiniAppCustomPermissionType.CONTACT_LIST, "reason"),
                Pair(MiniAppCustomPermissionType.ACCESS_TOKEN, "reason")
            ), TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
        )
        miniAppViewHandler.isManifestEqual(dummyApiManifest, dummyManifest) shouldBeEqualTo false

        val diffMetaManifest = MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
            listOf(),
            TEST_ATP_LIST,
            mapOf(Pair("abc", "bcd")),
            TEST_MA_VERSION_ID
        )
        miniAppViewHandler.isManifestEqual(diffMetaManifest, dummyManifest) shouldBeEqualTo false

        val diffReqPermManifest = MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "reason")),
            listOf(),
            TEST_ATP_LIST,
            mapOf(),
            TEST_MA_VERSION_ID
        )
        miniAppViewHandler.isManifestEqual(diffReqPermManifest, dummyManifest) shouldBeEqualTo false

        val diffOptPermManifest = MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
            listOf(Pair(MiniAppCustomPermissionType.CONTACT_LIST, "reason")),
            TEST_ATP_LIST,
            mapOf(Pair("abc", "bcd")),
            TEST_MA_VERSION_ID
        )
        miniAppViewHandler.isManifestEqual(diffOptPermManifest, dummyManifest) shouldBeEqualTo false
    }

    @Test
    fun `isManifestEqual will return false when api and downloaded manifest are null`() {
        miniAppViewHandler.isManifestEqual(null, null) shouldBeEqualTo false
    }

    /** end region */

    @Test
    fun `api manifest should be fetched from MiniAppDownloader`() = runBlockingTest {
        miniAppViewHandler.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID, languageCode)
        verify(miniAppViewHandler.miniAppDownloader).fetchMiniAppManifest(
            TEST_MA_ID, TEST_MA_VERSION_ID, languageCode
        )
    }

    @Test
    fun `api manifest should not be fetched from MiniAppDownloader when different languageCode`() =
        runBlockingTest {
            miniAppViewHandler.getMiniAppManifest(
                TEST_MA_ID, TEST_MA_VERSION_ID, languageCode
            )
            verify(miniAppViewHandler.miniAppDownloader, times(0)).fetchMiniAppManifest(
                TEST_MA_ID, TEST_MA_VERSION_ID, ""
            )
        }

    /** region: RealMiniApp.create */
    private fun onGettingManifestWhileCreate() = runBlockingTest {
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
        When calling miniAppViewHandler.downloadedManifestCache
            .readDownloadedManifest(TEST_MA_ID) itReturns cachedManifest
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
                false,
                miniAppViewHandler.miniAppIAPVerifier
            )
        }

    @Test
    @Suppress("LongMethod")
    fun `should invoke createMiniAppDisplay when when all checks passed`() =
        runBlockingTest {
            withMiniAppViewHandler { _, testMiniAppInfo ->
                When calling miniAppViewHandler.miniAppStorage.isValidMiniAppInfo(
                    TEST_MA_ID,
                    TEST_MA_VERSION_ID
                ) itReturns true
                When calling miniAppViewHandler.miniAppStorage.isMiniAppAvailable(
                    TEST_MA_ID,
                    TEST_MA_VERSION_ID
                ) itReturns true
                When calling miniAppViewHandler.miniAppVerifier.verify(any(), any()) itReturns true

                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit =
                    { display, _ ->
                        assertEquals(display, miniAppViewHandler.displayer.createMiniAppDisplay(
                            "test/path",
                            testMiniAppInfo,
                            miniAppConfig.miniAppMessageBridge,
                            null,
                            null,
                            miniAppViewHandler.miniAppCustomPermissionCache,
                            miniAppViewHandler.downloadedManifestCache,
                            "",
                            miniAppViewHandler.miniAppAnalytics,
                            miniAppViewHandler.ratDispatcher,
                            miniAppViewHandler.secureStorageDispatcher,
                            false,
                            miniAppViewHandler.miniAppIAPVerifier
                        ))
                    }
                miniAppViewHandler.createMiniAppViewFromBundle(testMiniAppInfo, miniAppConfig, null, onComplete)
            }
        }

    @Test
    @Suppress("LongMethod")
    fun `should invoke MiniAppBundleNotFoundException when the bundle is not available in files`() =
        runBlockingTest {
            withMiniAppViewHandler { _, testMiniAppInfo ->
                When calling miniAppViewHandler.miniAppStorage.isValidMiniAppInfo(
                    TEST_MA_ID,
                    TEST_MA_VERSION_ID
                ) itReturns true
                When calling miniAppViewHandler.miniAppStorage.isMiniAppAvailable(
                    TEST_MA_ID,
                    TEST_MA_VERSION_ID
                ) itReturns false

                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit =
                    { _, miniAppSdkException ->
                        assertTrue { miniAppSdkException is MiniAppBundleNotFoundException }
                    }
                miniAppViewHandler.createMiniAppViewFromBundle(testMiniAppInfo, miniAppConfig, null, onComplete)
            }
        }

    @Suppress("LongMethod")
    fun `should invoke InvalidMiniAppInfoException when id and versionId invalid`() =
        runBlockingTest {
            withMiniAppViewHandler { _, testMiniAppInfo ->
                When calling miniAppViewHandler.miniAppStorage.isValidMiniAppInfo(
                    TEST_MA_ID,
                    TEST_MA_VERSION_ID
                ) itReturns false
                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit =
                    { _, miniAppSdkException ->
                        assertTrue { miniAppSdkException is InvalidMiniAppInfoException }
                    }
                miniAppViewHandler.createMiniAppViewFromBundle(testMiniAppInfo, miniAppConfig, null, onComplete)
            }
        }

    @Suppress("LongMethod")
    fun `should invoke MiniAppHasCorruptedException when verify hash has failed`() =
        runBlockingTest {
            withMiniAppViewHandler { _, testMiniAppInfo ->
                When calling miniAppViewHandler.miniAppStorage.isValidMiniAppInfo(
                    TEST_MA_ID,
                    TEST_MA_VERSION_ID
                ) itReturns true
                When calling miniAppViewHandler.miniAppStorage.isMiniAppAvailable(
                    TEST_MA_ID,
                    TEST_MA_VERSION_ID
                ) itReturns true
                When calling miniAppViewHandler.miniAppVerifier.verify(any(), any()) itReturns false

                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit =
                    { _, miniAppSdkException ->
                        assertTrue { miniAppSdkException is MiniAppHasCorruptedException }
                    }
                miniAppViewHandler.createMiniAppViewFromBundle(testMiniAppInfo, miniAppConfig, null, onComplete)
            }
        }

    private suspend fun withMiniAppViewHandler(onReady: suspend (Pair<String, MiniAppInfo>, MiniAppInfo) -> Unit) {
        onGettingManifestWhileCreate()
        val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
        val testVersion = Version("", TEST_MA_VERSION_ID)
        val testMiniAppInfo = TEST_MA.copy(id = TEST_MA_ID, version = testVersion)
        val testVersionPath = "test/path"

        When calling miniAppViewHandler.miniAppDownloader.getMiniApp(testMiniAppInfo) itReturns getMiniAppResult
        When calling miniAppViewHandler.miniAppStorage.getBundleWritePath(
            TEST_MA_ID,
            TEST_MA_VERSION_ID
        ) itReturns testVersionPath
        When calling miniAppConfig.miniAppMessageBridge itReturns miniAppMessageBridge
        When calling miniAppConfig.queryParams itReturns ""
        onReady(getMiniAppResult, testMiniAppInfo)
    }

    @Test
    @Suppress("LongMethod")
    fun `should invoke MiniAppDownloader, Displayer and verifyManifest while mini app view creation by miniAppInfo`() =
        runBlockingTest {

            withMiniAppViewHandler { getMiniAppResult, testMiniAppInfo ->
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
                    false,
                    miniAppViewHandler.miniAppIAPVerifier
                )
            }
        }

    @Test(expected = NullPointerException::class)
    @Suppress("LongMethod")
    fun `should invoke displayer createMiniAppDisplay while mini app view creation by miniAppInfo from cache`() =
        runBlockingTest {
            withMiniAppViewHandler { getMiniAppResult, testMiniAppInfo ->
                val cachedMiniApp: Pair<String, MiniAppInfo> = mock()
                When calling miniAppViewHandler.miniAppDownloader.getCachedMiniApp(TEST_MA_ID) itReturns cachedMiniApp

                miniAppViewHandler.createMiniAppView(testMiniAppInfo, miniAppConfig, true)

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
                    false,
                    miniAppViewHandler.miniAppIAPVerifier

                )
            }
        }

    @Test
    fun `should invoke getCachedMiniApp while mini app view creation if fromCache is true`() =
        runBlockingTest {
            onGettingManifestWhileCreate()
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppViewHandler.miniAppDownloader.getCachedMiniApp(TEST_MA_ID) itReturns getMiniAppResult
            When calling miniAppConfig.miniAppMessageBridge itReturns mock()
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
            When calling miniAppViewHandler.apiClient itReturns mock()
            When calling miniAppConfig.miniAppMessageBridge itReturns mock()
            miniAppViewHandler.createMiniAppView(TEST_MA_ID, miniAppConfig, false)
            verify(miniAppViewHandler.miniAppDownloader).getMiniApp(TEST_MA_ID)
        }

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
                false,
                miniAppViewHandler.miniAppIAPVerifier
            )
        }
    /** end region */
}
