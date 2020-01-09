package com.rakuten.tech.mobile.miniapp.api

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
    testValue: String = "test_value"
) = MockResponse().setBody("""
        {
            "testKey": "$testValue"
        }
    """.trimIndent())
