package com.rakuten.tech.mobile.miniapp.signatureverifier.api

import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.miniapp.signatureverifier.PublicKeyFetcher
import junit.framework.TestCase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.argForWhich
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

open class PublicKeyFetcherSpec {
    internal val mockApiClient = Mockito.mock(SignatureApiClient::class.java)

    internal fun createFetcher() =
            PublicKeyFetcher(mockApiClient)

    internal fun enqueueResponse(
        body: String,
        code: Int
    ) {
        When calling mockApiClient.fetchPath(any(), eq(null)) itReturns Response.Builder()
            .request(Request.Builder().url("https://www.example.com").build())
            .protocol(Protocol.HTTP_2)
            .message("")
            .code(code)
            .body(body.toResponseBody("text/plain; charset=utf-8".toMediaType()))
            .build()
    }
}

class PublicKeyFetcherNormalSpec : PublicKeyFetcherSpec() {

    private fun enqueueSuccessResponse(
        id: String = "test_id",
        ecKey: String = "test_key",
        pemKey: String = "test_pemkey"
    ) {
        enqueueResponse(
            body = """
                {
                    "id": "$id",
                    "ecKey": "$ecKey",
                    "pemKey": "$pemKey"
                }
            """.trimIndent(),
            code = 200
        )
    }

    @Test
    fun `should fetch the public key`() {
        val fetcher = createFetcher()
        enqueueSuccessResponse(ecKey = "test_key")

        fetcher.fetch("test_key_id") shouldBeEqualTo "test_key"
    }

    @Test
    fun `should fetch the public key for the provided key id`() {
        val fetcher = createFetcher()
        enqueueSuccessResponse(id = "test_key_id")

        fetcher.fetch("test_key_id")

        Verify on mockApiClient that mockApiClient.fetchPath(argForWhich {
            contains("test_key_id")
        }, eq(null))
    }

    @Test
    fun `should parse response correctly`() {
        val response = PublicKeyResponse.fromJsonString(SAMPLE_RESPONSE)
        response.id shouldBeEqualTo "test_id"
        response.ecKey shouldBeEqualTo "test_key"
        response.pemKey shouldBeEqualTo "test_pemKey"
    }

    companion object {
        private val SAMPLE_RESPONSE =
            """{
                "id": "test_id",
                "ecKey": "test_key",
                "pemKey": "test_pemKey"
            }""".trimIndent()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PublicKeyFetcherErrorSpec : PublicKeyFetcherSpec() {

    private fun enqueueErrorResponse(
        body: String = "",
        code: Int = 200
    ) {
        enqueueResponse(
            body = body,
            code = code
        )
    }

    @Test(expected = IOException::class)
    fun `should throw when the request is unsuccessful`() {
        enqueueErrorResponse(code = 400)

        val fetcher = createFetcher()

        fetcher.fetch("test_key_id")
    }

    @Test
    fun `should return empty even if 'id' key is missing in response`() {
        enqueueErrorResponse(
            body = """{
                "ecKey": "test_key",
                "pemKey": "test_pemKey"
            }""".trimIndent()
        )
        val fetcher = createFetcher()

        fetcher.fetch("test_key_id").shouldBeEmpty()
    }

    @Test
    fun `should return empty when the 'ecKey' key is missing in response`() {
        enqueueErrorResponse(
            body = """{
                "id": "test_key_id",
                "pemKey": "test_pemKey"
            }""".trimIndent()
        )
        val fetcher = createFetcher()

        fetcher.fetch("test_key_id").shouldBeEmpty()
    }

    @Test
    fun `should return valid key even if 'pemKey' key is missing in response`() {
        enqueueErrorResponse(
            body = """{
                "id": "test_key_id",
                "ecKey": "test_key"
            }""".trimIndent()
        )
        val fetcher = createFetcher()

        fetcher.fetch("test_key_id") shouldBeEqualTo "test_key"
    }

    @Test(expected = IOException::class)
    fun `should throw when valid client but invalid url`() {
        enqueueErrorResponse(code = 404)

        val fetcher = PublicKeyFetcher(
                SignatureApiClient(
                        "https://www.example.com",
                        "key",
                        ApplicationProvider.getApplicationContext()
                )
        )

        fetcher.fetch("test_key_id")
    }

    @Test
    @Suppress("TooGenericExceptionCaught")
    fun `should not throw when there are extra keys in the response`() {
        enqueueErrorResponse(
            body = """{
                "id": "test_id",
                "ecKey": "test_key",
                "pemKey": "test_pemKey",
                "randomKey": "random_value"
            }""".trimIndent()
        )
        val fetcher = createFetcher()

        try {
            fetcher.fetch("test_key_id")
        } catch (e: Exception) {
            TestCase.fail("Should not throw an exception.")
            throw e
        }
    }

    @Test
    fun `should be empty if invalid json format`() {
        val response = PublicKeyResponse.fromJsonString(INVALID_RESPONSE)
        response.id.shouldBeEmpty()
        response.ecKey.shouldBeEmpty()
        response.ecKey.shouldBeEmpty()
    }

    companion object {
        private val INVALID_RESPONSE =
            """{
                "some": "test_id",
                "other": "test_key",
                "values": "test
            }""".trimIndent()
    }
}
