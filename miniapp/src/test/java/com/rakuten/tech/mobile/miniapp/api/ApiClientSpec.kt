package com.rakuten.tech.mobile.miniapp.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase
import kotlinx.coroutines.test.runBlockingTest
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
import java.util.UUID

open class ApiClientSpec {

    private val mockRetrofitClient: Retrofit = mock()
    private val mockRequestExecutor: RetrofitRequestExecutor = mock()
    private val mockListingApi: ListingApi = mock()
    private val mockManifestApi: ManifestApi = mock()

    @Test
    fun `should fetch the list of mini apps`() = runBlockingTest {
        val listingEntity = ListingEntity(
            id = "test_id",
            versionId = "test_version",
            name = "test_name",
            description = "test_description",
            icon = "test_icon",
            files = listOf("https://www.example.com")
        )
        val mockCall: Call<List<ListingEntity>> = mock()
        When calling mockListingApi.list(any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns listOf(listingEntity)

        val apiClient = createApiClient(listingApi = mockListingApi)

        apiClient.list()[0] shouldEqual listingEntity
    }

    @Test
    fun `should fetch the file list of a mini app`() = runBlockingTest {
        val fileList = listOf("https://www.example.com", "https://www.example1.com")
        val manifestEntity = ManifestEntity(fileList)
        val mockCall: Call<ManifestEntity> = mock()
        When calling
                mockManifestApi
                    .fetchFileListFromManifest(any(), any()) itReturns mockCall
        When calling mockRequestExecutor.executeRequest(mockCall) itReturns ManifestEntity(fileList)

        val apiClient = createApiClient(manifestApi = mockManifestApi)

        apiClient
            .fetchFileList(UUID.randomUUID(), UUID.randomUUID()) shouldEqual manifestEntity
    }

    private fun createApiClient(
        retrofit: Retrofit = mockRetrofitClient,
        hostAppVersion: String = "test_version",
        requestExecutor: RetrofitRequestExecutor = mockRequestExecutor,
        listingApi: ListingApi = mockListingApi,
        manifestApi: ManifestApi = mockManifestApi
    ) = ApiClient(
        retrofit = retrofit,
        hostAppVersion = hostAppVersion,
        requestExecutor = requestExecutor,
        listingApi = listingApi,
        manifestApi = manifestApi
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

    @Test(expected = MiniAppHttpException::class)
    fun `should throw when server returns error response`() = runBlockingTest {
        mockServer.enqueue(createErrorResponse())

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
        } catch (exception: MiniAppHttpException) {
            exception.errorMessage shouldEqual "error_message"
        }
    }

    @Test
    fun `should append error message to base exception message`() = runBlockingTest {
        mockServer.enqueue(createErrorResponse(message = "error_message"))

        try {
            createRequestExecutor()
                .executeRequest(createApi().fetch())

            TestCase.fail("Should have thrown ErrorResponseException.")
        } catch (exception: MiniAppHttpException) {
            exception.message() shouldContain "error_message"
        }
    }

    @Test
    fun `should append default message when server doesn't return error message`() = runBlockingTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("{}")
        )

        try {
            createRequestExecutor()
                .executeRequest(createApi().fetch())

            TestCase.fail("Should have thrown ErrorResponseException.")
        } catch (exception: MiniAppHttpException) {
            exception.message() shouldContain "No error message"
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
