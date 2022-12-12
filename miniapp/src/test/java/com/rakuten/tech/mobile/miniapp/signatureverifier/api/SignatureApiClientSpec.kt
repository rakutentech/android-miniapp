package com.rakuten.tech.mobile.miniapp.signatureverifier.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.miniapp.RobolectricBaseSpec
import com.rakuten.tech.mobile.miniapp.TEST_BODY
import com.rakuten.tech.mobile.miniapp.TEST_PATH
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.logging.Level
import java.util.logging.LogManager



class SignatureApiClientSpec : RobolectricBaseSpec() {

    private val server = MockWebServer()
    private val mockContext = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var baseUrl: String

    init {
        LogManager.getLogManager()
            .getLogger(MockWebServer::class.java.name).level = Level.OFF
    }

    @Before
    fun setup() {
        server.start()
        baseUrl = server.url("client").toString()
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `should fetch the response`() {
        val client = createClient()
        enqueueResponse("test_body")

        client.fetchPath(TEST_PATH, null).body!!.string() shouldBeEqualTo "test_body"
    }

    @Test
    fun `should fetch from the base url`() {
        val client = createClient()
        enqueueResponse()

        client.fetchPath(TEST_PATH, null)

        server.takeRequest().requestUrl.toString() shouldStartWith baseUrl
    }

    @Test
    fun `should fetch using the provided path`() {
        val client = createClient()
        enqueueResponse()

        client.fetchPath("test/path/to/fetch", null)

        server.takeRequest().requestUrl.toString() shouldContain "/test/path/to/fetch"
    }

    @Test
    fun `should attach ETag value to subsequent requests as If-None-Match`() {
        val client = createClient()
        enqueueResponse(etag = "etag_value")
        enqueueResponse()

        client.fetchPath(TEST_PATH, null).body!!.string()
        server.takeRequest()
        client.fetchPath(TEST_PATH, null).body!!.string()

        server.takeRequest().headers["If-None-Match"] shouldBeEqualTo "etag_value"
    }

    @Test
    fun `should return cached body for 304 response code`() {
        val client = createClient()
        enqueueResponse(TEST_BODY)
        server.enqueue(MockResponse().setResponseCode(304))

        client.fetchPath(TEST_PATH, null).body!!.string()

        client.fetchPath(TEST_PATH, null).body!!.string() shouldBeEqualTo TEST_BODY
    }

    @Test
    fun `should cache the body between App launches`() {
        enqueueResponse(TEST_BODY)
        server.enqueue(MockResponse().setResponseCode(304))

        createClient().fetchPath(TEST_PATH, null).body!!.string()

        createClient().fetchPath(TEST_PATH, null).body!!.string() shouldBeEqualTo TEST_BODY
    }

    @Test
    fun `should include all query parameter`() {
        val paramMap = HashMap<String, String>()
        paramMap["key1"] = "value1"
        paramMap["key2"] = "value2"
        val client = SignatureApiClient(baseUrl, "key", mockContext)

        enqueueResponse(TEST_BODY)
        server.enqueue(MockResponse().setResponseCode(304))
        val response = client.fetchPath(TEST_PATH, paramMap)

        for ((k, v) in paramMap) response.request.url.queryParameter(k) shouldBe v
    }

    @Test
    fun `should include have empty value query param`() {
        val paramMap = HashMap<String, String>()
        paramMap["key1"] = "value1"
        paramMap["key2"] = ""
        val client = SignatureApiClient(baseUrl, "key", mockContext)

        enqueueResponse(TEST_BODY)
        server.enqueue(MockResponse().setResponseCode(304))
        val response = client.fetchPath(TEST_PATH, paramMap)

        response.request.url.queryParameterNames.size shouldBe 1
        response.request.url.queryParameter("key1") shouldBe "value1"
        response.request.url.queryParameter("key2").shouldBeNull()
    }

    @Test
    fun `should include have empty key query param`() {
        val paramMap = HashMap<String, String>()
        paramMap["key1"] = "value1"
        paramMap[""] = "value2"
        val client = SignatureApiClient(baseUrl, "key", mockContext)

        enqueueResponse(TEST_BODY)
        server.enqueue(MockResponse().setResponseCode(304))
        val response = client.fetchPath(TEST_PATH, paramMap)

        response.request.url.queryParameterNames.size shouldBe 1
        response.request.url.queryParameter("key1") shouldBe "value1"
        response.request.url.queryParameter("").shouldBeNull()
    }

    @Test
    fun `should include have empty key value query param`() {
        val paramMap = HashMap<String, String>()
        paramMap["key1"] = "value1"
        paramMap[""] = ""
        val client = SignatureApiClient(baseUrl, "key", mockContext)

        enqueueResponse(TEST_BODY)
        server.enqueue(MockResponse().setResponseCode(304))
        val response = client.fetchPath(TEST_PATH, paramMap)

        response.request.url.queryParameterNames.size shouldBe 1
        response.request.url.queryParameter("key1") shouldBe "value1"
        response.request.url.queryParameter("").shouldBeNull()
    }

    @Test
    fun `should not include empty value`() {
        val paramMap = HashMap<String, String>()
        val client = SignatureApiClient(baseUrl, "key", mockContext)

        enqueueResponse(TEST_BODY)
        server.enqueue(MockResponse().setResponseCode(304))
        val response = client.fetchPath(TEST_PATH, paramMap)

        response.request.url.queryParameterNames.shouldBeEmpty()
    }

    @Test(expected = Exception::class)
    fun `should throw when an invalid base url is provided`() {
        createClient(url = "invalid url")
    }

    private fun enqueueResponse(body: String = "test_body", etag: String = "etag_value") {
        server.enqueue(
            MockResponse()
                .setBody(body)
                .setHeader("ETag", etag)
        )
    }

    private fun createClient(url: String = baseUrl) =
        SignatureApiClient(url, "key", ApplicationProvider.getApplicationContext())
}
