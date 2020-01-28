package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.TEST_VALUE
import okhttp3.mockwebserver.MockResponse
import retrofit2.Call
import retrofit2.http.GET

interface TestApi {
    @GET("testendpoint")
    fun fetch(): Call<TestResponse>
}

data class TestResponse(
    val testKey: String
)

fun createTestApiResponse(
    testValue: String = TEST_VALUE
) = MockResponse().setBody(
    """
        {
            "testKey": "$testValue"
        }
    """.trimIndent()
)

fun createInvalidTestApiResponse(
    testValue: String = TEST_VALUE
) = MockResponse().setBody(
    """
        {
            "testKey": {"$testValue"},
        }
    """.trimIndent()
)
