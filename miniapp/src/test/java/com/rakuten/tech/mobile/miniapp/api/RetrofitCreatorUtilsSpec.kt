package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.TEST_LIST_PUBLIC_KEY_SSL
import com.rakuten.tech.mobile.miniapp.TEST_URL_HTTPS_1
import org.mockito.kotlin.mock
import com.rakuten.tech.mobile.miniapp.TEST_VALUE
import okhttp3.CertificatePinner
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException

class RetrofitCreatorUtilsSpec private constructor(
    private val mockServer: MockWebServer
) : MockWebServerBaseSpec(mockServer) {

    constructor() : this(MockWebServer())

    private val mockRasSdkHeaders: RasSdkHeaders = mock()
    private lateinit var baseUrl: String

    @Before
    fun setup() {
        baseUrl = mockServer.url("/").toString()

        mockServer.enqueue(createTestApiResponse())

        When calling mockRasSdkHeaders.asArray() itReturns emptyArray()
    }

    @Test
    fun `should attach the RAS headers to requests`() {
        When calling mockRasSdkHeaders.asArray() itReturns arrayOf("ras_header_name" to "ras_header_value")

        executeCreateClient()

        mockServer.takeRequest().getHeader("ras_header_name") shouldBeEqualTo "ras_header_value"
    }

    @Test
    fun `should attach the required Accept-Encoding type to requests`() {
        executeCreateClient()

        mockServer.takeRequest().getHeader("Accept-Encoding") shouldBeEqualTo "identity"
    }

    private fun executeCreateClient() = createClient()
        .create(TestApi::class.java)
        .fetch()
        .execute()

    @Test
    fun `should parse a JSON response`() {
        mockServer.enqueue(createTestApiResponse(testValue = TEST_VALUE))

        val response = executeCreateClient()

        response.body()!!.testKey shouldBeEqualTo TEST_VALUE
    }

    @Test
    fun `extract base url should return the authority correctly`() {
        val authority = extractBaseUrl(TEST_URL_HTTPS_1)
        authority shouldBeEqualTo "www.example.com"
    }

    @Test
    fun `extract base url should return empty string if invalid url passed`() {
        val authority = extractBaseUrl("TEST_URL_HTTPS")
        authority shouldBeEqualTo ""
    }

    @Test
    fun `extract base url should return empty string if malformed url passed`() {
        val authority = extractBaseUrl("http://example.com:-80/")
        authority shouldBeEqualTo ""
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create certificate pinner should return exception for wrong public key format`() {
        createCertificatePinner(baseUrl = TEST_URL_HTTPS_1, pubKeyList = listOf(""))
    }

    @Test
    fun `create certificate pinner should return correct certificate pinner`() {
        val actualPinner = createCertificatePinner(baseUrl = TEST_URL_HTTPS_1, pubKeyList = listOf("sha1/XXX-XXX"))
        val certificatePinnerBuilder = CertificatePinner.Builder()
        certificatePinnerBuilder.add(extractBaseUrl(TEST_URL_HTTPS_1), "sha1/XXX-XXX")
        val expectedPinner = certificatePinnerBuilder.build()
        actualPinner shouldBeEqualTo expectedPinner
    }

    private fun createClient() = createRetrofitClient(
        baseUrl = baseUrl,
        pubKeyList = TEST_LIST_PUBLIC_KEY_SSL,
        headers = mockRasSdkHeaders
    )
}
