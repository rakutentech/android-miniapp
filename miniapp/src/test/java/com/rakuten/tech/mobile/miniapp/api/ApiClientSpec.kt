package com.rakuten.tech.mobile.miniapp.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.sdkutils.AppInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.*
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit

@ExperimentalCoroutinesApi
open class ApiClientSpec {

    private val mockRetrofitClient: Retrofit = mock()
    private val mockRequestExecutor: RetrofitRequestExecutor = mock()
    private val mockAppInfoApi: AppInfoApi = mock()
    private val mockManifestApi: ManifestApi = mock()
    private val mockMetadataApi: MetadataApi = mock()
    private val mockDownloadApi: DownloadApi = mock()

    private val miniAppInfo = MiniAppInfo(
        id = TEST_MA_ID,
        displayName = TEST_MA_DISPLAY_NAME,
        icon = TEST_MA_ICON,
        version = Version(TEST_MA_VERSION_TAG, TEST_MA_VERSION_ID)
    )

    @Test
    fun `should fetch the list of mini apps`() = runBlockingTest {
        val mockCall: Call<List<MiniAppInfo>> = mock()

        When calling mockAppInfoApi.list(any(), any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns listOf(miniAppInfo)

        val apiClient = createApiClient(appInfoApi = mockAppInfoApi)
        apiClient.list()[0] shouldEqual miniAppInfo
    }

    @Test
    fun `should fetch the file list of a mini app`() = runBlockingTest {
        val fileList = listOf(TEST_URL_HTTPS_1, TEST_URL_HTTPS_2)
        val manifestEntity = ManifestEntity(fileList)
        val mockCall: Call<ManifestEntity> = mock()
        When calling
                mockManifestApi
                    .fetchFileListFromManifest(any(), any(), any(), any()) itReturns mockCall
        When calling
                mockRequestExecutor
                    .executeRequest(mockCall) itReturns ManifestEntity(fileList)

        createApiClient(manifestApi = mockManifestApi).apply {
            fetchFileList(
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION
            ) shouldEqual manifestEntity
        }
    }

    @Test
    fun `should download a file from the given url`() = runBlockingTest {
        val mockCall: Call<ResponseBody> = mock()
        val mockResponseBody = TEST_BODY_CONTENT.toResponseBody(null)
        When calling
                mockDownloadApi
                    .downloadFile(TEST_URL_FILE) itReturns mockCall
        When calling
                mockRequestExecutor
                    .executeRequest(mockCall) itReturns mockResponseBody

        val apiClient = createApiClient()
        val response = apiClient
            .downloadFile(TEST_URL_FILE) shouldEqual mockResponseBody
        response.contentLength() shouldEqual mockResponseBody.contentLength()
    }

    @Test
    fun `should fetch meta data for a mini app for a given appId`() = runBlockingTest {
        val mockCall: Call<List<MiniAppInfo>> = mock()

        When calling mockAppInfoApi.fetchInfo(any(), any(), any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns listOf(miniAppInfo)

        val apiClient = createApiClient(appInfoApi = mockAppInfoApi)
        apiClient.fetchInfo(TEST_MA_ID) shouldEqual miniAppInfo
    }

    @Test
    fun `fetchInfo should return only the first item`() = runBlockingTest {
        val mockCall: Call<List<MiniAppInfo>> = mock()
        val secondItem = miniAppInfo.copy()
        val resultList = listOf(miniAppInfo, secondItem)

        When calling mockAppInfoApi.fetchInfo(any(), any(), any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns resultList

        val apiClient = createApiClient(appInfoApi = mockAppInfoApi)
        apiClient.fetchInfo(TEST_MA_ID) shouldNotBe secondItem
    }

    @Test(expected = MiniAppHasNoPublishedVersionException::class)
    fun `fetchInfo should throw MiniAppHasNoPublishedVersionException when the API returns zero items`() =
            runBlockingTest {
        val mockCall: Call<List<MiniAppInfo>> = mock()

        When calling mockAppInfoApi.fetchInfo(any(), any(), any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns emptyList()

        val apiClient = createApiClient(appInfoApi = mockAppInfoApi)
        apiClient.fetchInfo("test-app-id")
    }

    @Test
    fun `custom error response should extend the same error class`() {
        val errorClass = ErrorResponse::class.java
        HttpErrorResponse(0, "") shouldBeInstanceOf errorClass
        AuthErrorResponse("", "") shouldBeInstanceOf errorClass
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when call a request failed`() = runBlockingTest {
        When calling mockRetrofitClient.create(AppInfoApi::class.java) itReturns mockAppInfoApi
        When calling mockRetrofitClient.create(ManifestApi::class.java) itReturns mockManifestApi
        When calling mockRetrofitClient.create(MetadataApi::class.java) itReturns mockMetadataApi
        When calling mockRetrofitClient.create(DownloadApi::class.java) itReturns mockDownloadApi

        ApiClient(mockRetrofitClient, false, TEST_HA_ID_PROJECT).downloadFile(TEST_URL_FILE)
    }

    @Test
    fun `should create ApiClient without error`() {
        AppInfo.instance = mock()
        ApiClient(
            baseUrl = TEST_URL_HTTPS_2,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY
        )
    }

    @Test
    fun `MiniAppHttpException should provide proper message`() {
        val exception = MiniAppHttpException(
            Response.error(400, TEST_ERROR_MSG.toResponseBody()), TEST_ERROR_MSG)

        exception.message() shouldContain TEST_ERROR_MSG
        exception.errorMessage shouldBe TEST_ERROR_MSG
    }

    private fun createApiClient(
        retrofit: Retrofit = mockRetrofitClient,
        hostId: String = TEST_HA_ID_PROJECT,
        requestExecutor: RetrofitRequestExecutor = mockRequestExecutor,
        appInfoApi: AppInfoApi = mockAppInfoApi,
        manifestApi: ManifestApi = mockManifestApi,
        metadataApi: MetadataApi = mockMetadataApi,
        downloadApi: DownloadApi = mockDownloadApi
    ) = ApiClient(
        retrofit = retrofit,
        isPreviewMode = false,
        hostId = hostId,
        requestExecutor = requestExecutor,
        appInfoApi = appInfoApi,
        manifestApi = manifestApi,
        metadataApi = metadataApi,
        downloadApi = downloadApi
    )
}
