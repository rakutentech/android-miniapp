package com.rakuten.tech.mobile.miniapp.api

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.TEST_BODY_CONTENT
import com.rakuten.tech.mobile.miniapp.TEST_URL_FILE
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldContain
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.Charset

open class DownloadApiSpec private constructor(
    internal val mockServer: MockWebServer
) : MockWebServerBaseSpec(mockServer) {

    constructor() : this(MockWebServer())

    private lateinit var baseUrl: String
    internal lateinit var retrofit: Retrofit

    @Before
    fun baseSetup() {
        baseUrl = mockServer.url("/").toString()
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    internal fun createResponse(
        fileData: ByteArray = TEST_BODY_CONTENT.toByteArray(Charset.defaultCharset())
    ) = MockResponse().setBody(Gson().toJson(fileData))
}

class DownloadApiRequestSpec : DownloadApiSpec() {

    @Test
    fun `should be able to download a file from the 'download' endpoint`() {
        mockServer.enqueue(createResponse())

        retrofit.create(DownloadApi::class.java)
            .downloadFile(TEST_URL_FILE)
            .execute()

        mockServer.takeRequest().path.toString() shouldContain TEST_URL_FILE
    }
}
