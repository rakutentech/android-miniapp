package com.rakuten.tech.mobile.miniapp.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.rakuten.tech.mobile.miniapp.*
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@ExperimentalCoroutinesApi
open class ApiClientSpec {

    private val mockRetrofitClient: Retrofit = mock()
    private val mockRequestExecutor: RetrofitRequestExecutor = mock()
    private val mockListingApi: ListingApi = mock()
    private val mockManifestApi: ManifestApi = mock()
    private val mockDownloadApi: DownloadApi = mock()

    @Test
    fun `should fetch the list of mini apps`() = runBlockingTest {
        val miniAppInfo = MiniAppInfo(
            id = TEST_MA_ID,
            displayName = TEST_MA_DISPLAY_NAME,
            icon = TEST_MA_ICON,
            version = Version(TEST_MA_VERSION_TAG, TEST_MA_VERSION_ID)
        )
        val mockCall: Call<List<MiniAppInfo>> = mock()
        When calling mockListingApi.list(any(), any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns listOf(miniAppInfo)

        val apiClient = createApiClient(listingApi = mockListingApi)
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
        val mockResponseBody = ResponseBody.create(null, TEST_BODY_CONTENT)
        When calling
                mockDownloadApi
                    .downloadFile(TEST_URL_FILE) itReturns mockCall
        When calling
                mockRequestExecutor
                    .executeRequest(mockCall) itReturns mockResponseBody

        val apiClient = createApiClient(downloadApi = mockDownloadApi)
        val response = apiClient
            .downloadFile(TEST_URL_FILE) shouldEqual mockResponseBody
        response.contentLength() shouldEqual mockResponseBody.contentLength()
    }

    private fun createApiClient(
        retrofit: Retrofit = mockRetrofitClient,
        hostAppId: String = TEST_HA_ID_APP,
        hostAppVersionId: String = TEST_HA_ID_VERSION,
        requestExecutor: RetrofitRequestExecutor = mockRequestExecutor,
        listingApi: ListingApi = mockListingApi,
        manifestApi: ManifestApi = mockManifestApi,
        downloadApi: DownloadApi = mockDownloadApi
    ) = ApiClient(
        retrofit = retrofit,
        hostAppId = hostAppId,
        hostAppVersionId = hostAppVersionId,
        requestExecutor = requestExecutor,
        listingApi = listingApi,
        manifestApi = manifestApi,
        downloadApi = downloadApi
    )
}

open class RetrofitRequestExecutorSpec private constructor(
    internal val mockServer: MockWebServer
) : MockWebServerBaseTest(mockServer) {

    constructor() : this(MockWebServer())

    private lateinit var retrofit: Retrofit
    private lateinit var baseUrl: String

    @Before
    fun baseSetup() {
        baseUrl = mockServer.url("/").toString()

        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    internal fun createApi() = retrofit.create(TestApi::class.java)

    internal fun createRequestExecutor(
        retrofit: Retrofit = this.retrofit
    ) = RetrofitRequestExecutor(
        retrofit = retrofit
    )
}

@ExperimentalCoroutinesApi
open class RetrofitRequestExecutorNormalSpec : RetrofitRequestExecutorSpec() {

    @Test
    fun `should return the body`() = runBlockingTest {
        mockServer.enqueue(createTestApiResponse(testValue = TEST_VALUE))

        val response = createRequestExecutor()
            .executeRequest(createApi().fetch())

        response.testKey shouldEqual TEST_VALUE
    }
}

@ExperimentalCoroutinesApi
open class RetrofitRequestExecutorErrorSpec : RetrofitRequestExecutorSpec() {

    @Test(expected = MiniAppSdkException::class)
    fun `should throw when server returns error response`() = runBlockingTest {
        mockServer.enqueue(createErrorResponse())

        createRequestExecutor()
            .executeRequest(createApi().fetch())
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when the response returned by server is not of type T`() =
        runBlockingTest {
            mockServer.enqueue(createInvalidTestApiResponse())
            createRequestExecutor()
                .executeRequest(createApi().fetch())
        }

    @Test
    fun `should throw exception with the error message returned by server`() = runBlockingTest {
        mockServer.enqueue(createErrorResponse(message = TEST_ERROR_MSG))

        try {
            createRequestExecutor()
                .executeRequest(createApi().fetch())

            TestCase.fail("Should have thrown ErrorResponseException.")
        } catch (exception: MiniAppSdkException) {
            exception.message.toString() shouldContain TEST_ERROR_MSG
        }
    }

    @Test
    fun `should append error message to base exception message`() = runBlockingTest {
        mockServer.enqueue(createErrorResponse(message = TEST_ERROR_MSG))

        try {
            createRequestExecutor()
                .executeRequest(createApi().fetch())

            TestCase.fail("Should have thrown ErrorResponseException.")
        } catch (exception: MiniAppSdkException) {
            exception.message.toString() shouldContain TEST_ERROR_MSG
        }
    }

    @Test
    fun `should append default message when server doesn't return error message`() =
        runBlockingTest {
            mockServer.enqueue(
                MockResponse()
                    .setResponseCode(400)
                    .setBody("{}")
            )

            try {
                createRequestExecutor()
                    .executeRequest(createApi().fetch())

                TestCase.fail("Should have thrown ErrorResponseException.")
            } catch (exception: MiniAppSdkException) {
                exception.message.toString() shouldContain "No error message"
            }
        }

    private fun createErrorResponse(
        code: Int = 400,
        message: String = TEST_ERROR_MSG
    ): MockResponse {
        val error = """
            {
                "code": $code,
                "message": "$message"
            }
        """.trimIndent()

        return MockResponse()
            .setResponseCode(code)
            .setBody(error)
    }
}
