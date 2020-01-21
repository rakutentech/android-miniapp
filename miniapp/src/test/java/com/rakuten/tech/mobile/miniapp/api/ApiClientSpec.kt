package com.rakuten.tech.mobile.miniapp.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import junit.framework.TestCase
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

internal const val TEST_ID_MINIAPP = "5f0ed952-36ab-43e2-a285-b237c11e23cb"
internal const val TEST_ID_MINIAPP_VERSION = "fa7e1522-adf2-4322-8146-84dca1f812a5"
internal const val TEST_URL = "file.storage/test/file.abc"

open class ApiClientSpec {

    private val mockRetrofitClient: Retrofit = mock()
    private val mockRequestExecutor: RetrofitRequestExecutor = mock()
    private val mockListingApi: ListingApi = mock()
    private val mockManifestApi: ManifestApi = mock()
    private val mockDownloadApi: DownloadApi = mock()

    @Test
    fun `should fetch the list of mini apps`() = runBlockingTest {
        val miniAppInfo = MiniAppInfo(
            id = "test_id",
            versionId = "test_version",
            name = "test_name",
            description = "test_description",
            icon = "test_icon",
            files = listOf("https://www.example.com")
        )
        val mockCall: Call<List<MiniAppInfo>> = mock()
        When calling mockListingApi.list(any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns listOf(miniAppInfo)

        val apiClient = createApiClient(listingApi = mockListingApi)

        apiClient.list()[0] shouldEqual miniAppInfo
    }

    @Test
    fun `should fetch the file list of a mini app`() = runBlockingTest {
        val fileList = listOf("https://www.example.com", "https://www.example1.com")
        val manifestEntity = ManifestEntity(fileList)
        val mockCall: Call<ManifestEntity> = mock()
        When calling
                mockManifestApi
                    .fetchFileListFromManifest(any(), any()) itReturns mockCall
        When calling
                mockRequestExecutor
                    .executeRequest(mockCall) itReturns ManifestEntity(fileList)

        val apiClient = createApiClient(manifestApi = mockManifestApi)

        apiClient
            .fetchFileList(
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION
            ) shouldEqual manifestEntity
    }

    @Test
    fun `should download a file from the given url`() = runBlockingTest {
        val mockCall: Call<ResponseBody> = mock()
        val mockResponseBody = ResponseBody.create(null, "lorem ipsum")
        When calling
                mockDownloadApi
                    .downloadFile(TEST_URL) itReturns mockCall
        When calling
                mockRequestExecutor
                    .executeRequest(mockCall) itReturns mockResponseBody

        val apiClient = createApiClient(downloadApi = mockDownloadApi)
        val response = apiClient
            .downloadFile(TEST_URL) shouldEqual mockResponseBody
        response.contentLength() shouldEqual mockResponseBody.contentLength()
    }

    private fun createApiClient(
        retrofit: Retrofit = mockRetrofitClient,
        hostAppVersion: String = "test_version",
        requestExecutor: RetrofitRequestExecutor = mockRequestExecutor,
        listingApi: ListingApi = mockListingApi,
        manifestApi: ManifestApi = mockManifestApi,
        downloadApi: DownloadApi = mockDownloadApi
    ) = ApiClient(
        retrofit = retrofit,
        hostAppVersion = hostAppVersion,
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

open class RetrofitRequestExecutorNormalSpec : RetrofitRequestExecutorSpec() {

    @Test
    fun `should return the body`() = runBlockingTest {
        mockServer.enqueue(createTestApiResponse(testValue = "test_value"))

        val response = createRequestExecutor()
            .executeRequest(createApi().fetch())

        response.testKey shouldEqual "test_value"
    }
}

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
        mockServer.enqueue(createErrorResponse(message = "error_message"))

        try {
            createRequestExecutor()
                .executeRequest(createApi().fetch())

            TestCase.fail("Should have thrown ErrorResponseException.")
        } catch (exception: MiniAppSdkException) {
            exception.message.toString() shouldContain "error_message"
        }
    }

    @Test
    fun `should append error message to base exception message`() = runBlockingTest {
        mockServer.enqueue(createErrorResponse(message = "error_message"))

        try {
            createRequestExecutor()
                .executeRequest(createApi().fetch())

            TestCase.fail("Should have thrown ErrorResponseException.")
        } catch (exception: MiniAppSdkException) {
            exception.message.toString() shouldContain "error_message"
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
        message: String = "error_message"
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
