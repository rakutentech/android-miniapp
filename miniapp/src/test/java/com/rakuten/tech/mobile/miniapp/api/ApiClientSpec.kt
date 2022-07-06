package com.rakuten.tech.mobile.miniapp.api

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import com.rakuten.tech.mobile.miniapp.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.*
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import okhttp3.Headers.Companion.toHeaders

@ExperimentalCoroutinesApi
@SuppressWarnings("LargeClass")
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
        version = Version(TEST_MA_VERSION_TAG, TEST_MA_VERSION_ID),
        promotionalImageUrl = TEST_PROMOTIONAL_URL,
        promotionalText = TEST_PROMOTIONAL_TEXT
    )

    private val previewMiniAppInfo = PreviewMiniAppInfo(
        host = Host(id = TEST_HA_ID_PROJECT, subscriptionKey = TEST_HA_SUBSCRIPTION_KEY),
        miniapp = miniAppInfo
    )

    @Test
    fun `should fetch the list of mini apps`() = runBlockingTest {
        val mockCall: Call<List<MiniAppInfo>> = mock()
        val response: Response<List<MiniAppInfo>> = Response.success(listOf(miniAppInfo))

        When calling mockAppInfoApi.list(any(), any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns response

        val apiClient = createApiClient(appInfoApi = mockAppInfoApi)
        apiClient.list()[0] shouldBeEqualTo miniAppInfo
    }

    @Test
    fun `should fetch the file list of a mini app`() = runBlockingTest {
        val fileList = listOf(TEST_URL_HTTPS_1, TEST_URL_HTTPS_2)
        val manifestEntity = ManifestEntity(fileList, TEST_PUBLIC_KEY_ID)
        val mockCall: Call<ManifestEntity> = mock()
        val response: Response<ManifestEntity> =
            Response.success(ManifestEntity(fileList, TEST_PUBLIC_KEY_ID))
        When calling
                mockManifestApi
                    .fetchFileListFromManifest(any(), any(), any()) itReturns mockCall
        When calling
                mockRequestExecutor
                    .executeRequest(mockCall) itReturns response

        createApiClient(manifestApi = mockManifestApi).apply {
            fetchFileList(
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION
            ) shouldBeEqualTo Pair(manifestEntity, ManifestHeader(null))
        }
    }

    @Test
    fun `should fetch the signature of a mini app`() = runBlockingTest {
        val fileList = listOf(TEST_URL_HTTPS_1, TEST_URL_HTTPS_2)
        val mockCall: Call<ManifestEntity> = mock()
        val response: Response<ManifestEntity> =
            Response.success(
                ManifestEntity(fileList, TEST_PUBLIC_KEY_ID),
                mapOf("signature" to TEST_MANIFEST_SIGNATURE).toHeaders()
            )

        When calling
                mockManifestApi
                    .fetchFileListFromManifest(any(), any(), any()) itReturns mockCall
        When calling
                mockRequestExecutor
                    .executeRequest(mockCall) itReturns response

        createApiClient(manifestApi = mockManifestApi).apply {
            fetchFileList(
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION
            ).second.signature shouldBeEqualTo TEST_MANIFEST_SIGNATURE
        }
    }

    @Test
    fun `should download a file from the given url`() = runBlockingTest {
        val mockCall: Call<ResponseBody> = mock()
        val mockResponseBody = TEST_BODY_CONTENT.toResponseBody(null)
        val mockResponse: Response<ResponseBody> = Response.success(mockResponseBody)
        When calling
                mockDownloadApi
                    .downloadFile(TEST_URL_FILE) itReturns mockCall
        When calling
                mockRequestExecutor
                    .executeRequest(mockCall) itReturns mockResponse

        val apiClient = createApiClient()
        val response = apiClient
            .downloadFile(TEST_URL_FILE) shouldBeEqualTo mockResponseBody
        response.contentLength() shouldBeEqualTo mockResponseBody.contentLength()
    }

    @Test
    fun `should fetch meta data for a mini app for a given appId`() = runBlockingTest {
        val mockCall: Call<List<MiniAppInfo>> = mock()
        val response: Response<List<MiniAppInfo>> = Response.success(listOf(miniAppInfo))

        When calling mockAppInfoApi.fetchInfo(any(), any(), any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns response

        val apiClient = createApiClient(appInfoApi = mockAppInfoApi)
        apiClient.fetchInfo(TEST_MA_ID) shouldBeEqualTo miniAppInfo
    }

    @Test
    fun `fetchInfo should return only the first item`() = runBlockingTest {
        val mockCall: Call<List<MiniAppInfo>> = mock()
        val secondItem = miniAppInfo.copy()
        val resultList = listOf(miniAppInfo, secondItem)
        val response: Response<List<MiniAppInfo>> = Response.success(resultList)

        When calling mockAppInfoApi.fetchInfo(any(), any(), any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns response

        val apiClient = createApiClient(appInfoApi = mockAppInfoApi)
        apiClient.fetchInfo(TEST_MA_ID) shouldNotBe secondItem
    }

    @Test(expected = MiniAppHasNoPublishedVersionException::class)
    fun `fetchInfo should throw MiniAppHasNoPublishedVersionException when the API returns zero items`() =
        runBlockingTest {
            val mockCall: Call<List<MiniAppInfo>> = mock()
            val response: Response<List<MiniAppInfo>> = Response.success(emptyList())

            When calling mockAppInfoApi.fetchInfo(any(), any(), any()) itReturns mockCall
            When calling mockRequestExecutor.executeRequest(mockCall) itReturns response

            val apiClient = createApiClient(appInfoApi = mockAppInfoApi)
            apiClient.fetchInfo("test-app-id")
        }

    @Test
    fun `should fetch preview miniapp info data for a given preview code`() = runBlockingTest {
        val mockCall: Call<PreviewMiniAppInfo> = mock()
        val response: Response<PreviewMiniAppInfo> = Response.success(previewMiniAppInfo)

        When calling mockAppInfoApi.fetchInfoByPreviewCode(TEST_HA_ID_PROJECT, TEST_MA_PREVIEW_CODE) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns response

        val apiClient = createApiClient(appInfoApi = mockAppInfoApi)
        apiClient.fetchInfoByPreviewCode(TEST_MA_PREVIEW_CODE) shouldBeEqualTo previewMiniAppInfo
    }

    @Test
    fun `fetch miniapp manifest should return correct metadata entity`() = runBlockingTest {
        val metadataEntity: MetadataEntity = mock()
        val mockCall: Call<MetadataEntity> = mock()
        val response: Response<MetadataEntity> = Response.success(metadataEntity)

        When calling mockMetadataApi.fetchMetadata(TEST_HA_ID_PROJECT, TEST_MA_ID,
            TEST_MA_VERSION_ID, TEST_MA_LANGUAGE_CODE) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns response

        val apiClient = createApiClient(metadataApi = mockMetadataApi)
        apiClient.fetchMiniAppManifest(
            TEST_MA_ID,
            TEST_MA_VERSION_ID,
            TEST_MA_LANGUAGE_CODE
        ) shouldBeEqualTo metadataEntity
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

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when call body is null`() = runBlockingTest {
        val mockCall: Call<ResponseBody> = mock()
        val response: Response<ResponseBody> = mock()

        When calling mockDownloadApi.downloadFile(TEST_URL_FILE) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns response
        When calling response.body() itReturns null

        val apiClient = createApiClient(downloadApi = mockDownloadApi)
        apiClient.downloadFile(TEST_URL_FILE)
    }

    @Test
    fun `should create ApiClient without error`() {
        ApiClient(
            baseUrl = TEST_URL_HTTPS_2,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY
        )
    }

    @Test
    fun `MiniAppHttpException should provide proper message`() {
        val exception = MiniAppHttpException(
            Response.error(400, TEST_ERROR_MSG.toResponseBody()), TEST_ERROR_MSG
        )

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
