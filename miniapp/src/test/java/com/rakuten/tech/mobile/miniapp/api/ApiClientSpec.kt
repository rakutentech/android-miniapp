package com.rakuten.tech.mobile.miniapp.api

import junit.framework.TestCase
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class ApiClientSpec

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
