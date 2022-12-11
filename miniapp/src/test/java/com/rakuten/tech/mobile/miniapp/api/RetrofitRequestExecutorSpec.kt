package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.*
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Timeout
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class RetrofitRequestExecutorSpec private constructor(
    internal val mockServer: MockWebServer
) : MockWebServerBaseSpec(mockServer) {

    constructor() : this(MockWebServer())

    private lateinit var retrofit: Retrofit
    private lateinit var baseUrl: String
    internal val mockRequestExecutor: RetrofitRequestExecutor = mock()

    internal suspend fun fetchApi() = createRequestExecutor().executeRequest(createApi().fetch())

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

    internal fun spyRetrofitExecutor(): RetrofitRequestExecutor {
        val retrofit = createRetrofitClient(
            baseUrl = TEST_URL_HTTPS_2,
            pubKeyList = TEST_LIST_PUBLIC_KEY_SSL,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY
        )
        return Mockito.spy(RetrofitRequestExecutor(retrofit))
    }
}

@ExperimentalCoroutinesApi
@Suppress("EmptyFunctionBlock")
open class RetrofitRequestExecutorNormalSpec : RetrofitRequestExecutorSpec() {

    @Test
    fun `should return the body`() = runBlockingTest {
        mockServer.enqueue(createTestApiResponse(testValue = TEST_VALUE))

        val response = fetchApi()

        response.body()?.testKey shouldBeEqualTo TEST_VALUE
    }

    @Test
    fun `should call a request without error when the response is success`() = runBlockingTest {
        val executor = Mockito.spy(mockRequestExecutor)
        val request: Call<String> = SuccessfulResponseCall()
        executor.executeRequest(request)

        verify(executor).executeWithRetry(request)
    }
}

@ExperimentalCoroutinesApi
@Suppress("EmptyFunctionBlock")
open class RetrofitRequestExecutorErrorSpec : RetrofitRequestExecutorSpec() {

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when the response returned by server is not of type T`() =
        runBlockingTest {
            mockServer.enqueue(createInvalidTestApiResponse())
            fetchApi()
        }

    @Test
    fun `should throw exception with the error message returned by server`() = runBlockingTest {
        mockServer.enqueue(createErrorResponse(message = TEST_ERROR_MSG))

        try {
            fetchApi()

            TestCase.fail("Should have thrown ErrorResponseException.")
        } catch (exception: MiniAppSdkException) {
            exception.message.toString() shouldContain TEST_ERROR_MSG
        }
    }

    @Test
    fun `should append error message to base exception message`() = runBlockingTest {
        mockServer.enqueue(createErrorResponse(message = TEST_ERROR_MSG))

        try {
            fetchApi()

            TestCase.fail("Should have thrown ErrorResponseException.")
        } catch (exception: MiniAppSdkException) {
            exception.message.toString() shouldContain TEST_ERROR_MSG
        }
    }

    @Test
    fun `should append default message when server doesn't return error message`() =
        runBlockingTest {
            mockServer.enqueue(MockResponse().setResponseCode(406).setBody("{}"))

            try {
                fetchApi()

                TestCase.fail("Should have thrown ErrorResponseException.")
            } catch (exception: MiniAppSdkException) {
                exception.message.toString() shouldContain "No error message"
            }
        }

    @Test
    fun `should append default message when server returns 500 response code`() =
        runBlockingTest {
            try {
                mockServer.enqueue(MockResponse().setResponseCode(500))
                fetchApi()
            } catch (exception: MiniAppNetException) {
                exception.message.toString() shouldContain "Found some problem, timeout"
            }
        }

    @Test
    fun `should return the correct value for waiting time`() {
        val executor = spyRetrofitExecutor()
        val actual = executor.getWaitingTime(4)
        actual shouldBeEqualTo 8000
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when there is authentication errors`() = runBlockingTest {
        val executor = spyRetrofitExecutor()
        val request: Call<String> = Unauthorized401Call()
        executor.executeRequest(request)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when there is standard errors`() = runBlockingTest {
        val executor = spyRetrofitExecutor()
        val request: Call<String> = ResourceNotFoundCall()
        executor.executeRequest(request)
    }

    @Test(expected = MiniAppNetException::class)
    fun `should throw exception when there is network error`() = runBlockingTest {
        val executor = spyRetrofitExecutor()
        val request: Call<String> = UnknownHostCall()
        executor.executeRequest(request)
    }

    @Test(expected = MiniAppNetException::class)
    fun `should throw exception when there is timeout error`() = runBlockingTest {
        val executor = spyRetrofitExecutor()
        val request: Call<String> = SocketTimeOutCall()
        executor.executeRequest(request)
    }

    @Test(expected = MiniAppNotFoundException::class)
    fun `should throw MiniAppNotFoundException when the server returns 404`() = runBlockingTest {
        mockServer.enqueue(MockResponse().setResponseCode(404))

        fetchApi()
    }

    @Test(expected = MiniAppHostException::class)
    fun `should throw MiniAppHostException when the server returns 400`() = runBlockingTest {
        mockServer.enqueue(MockResponse().setResponseCode(400))

        fetchApi()
    }

    @Test(expected = MiniAppTooManyRequestsError::class)
    fun `should throw MiniAppTooManyRequestsError when the server returns 429`() = runBlockingTest {
        mockServer.enqueue(MockResponse().setResponseCode(429))

        fetchApi()
    }

    private val standardErrorBody = { code: Int, message: String ->
        """
            {
                "code": $code,
                "message": "$message"
            }
        """.trimIndent()
    }

    private fun createErrorResponse(
        code: Int = 406,
        message: String = TEST_ERROR_MSG
    ): MockResponse = MockResponse()
        .setResponseCode(code)
        .setBody(standardErrorBody(code, message))
}

class SuccessfulResponseCall : BaseCall() {
    override fun execute(): Response<String> = Response.success("")
}

class Unauthorized401Call : BaseCall() {
    override fun execute(): Response<String> = Response.error(401, mock())
}

class ResourceNotFoundCall : BaseCall() {
    override fun execute(): Response<String> = Response.error(404, mock())
}

class UnknownHostCall : BaseCall() {
    override fun execute(): Response<String> = throw UnknownHostException()
}

class SocketTimeOutCall : BaseCall() {
    override fun execute(): Response<String> = throw SocketTimeoutException()
}

@Suppress("EmptyFunctionBlock", "NotImplementedDeclaration")
open class BaseCall : Call<String> {

    override fun enqueue(callback: Callback<String>) {
        //do nothing intended
    }

    override fun isExecuted(): Boolean = true

    override fun timeout(): Timeout = Timeout.NONE

    override fun clone(): Call<String> = mock()

    override fun isCanceled(): Boolean = false

    override fun cancel() {
        //do nothing intended
    }

    @Suppress("TodoComment")
    override fun execute(): Response<String> = TODO("Not yet implemented")

    override fun request(): Request = Request.Builder().build()
}
