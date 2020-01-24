package com.rakuten.tech.mobile.miniapp.api

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEndWith
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldStartWith
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class ListingApiSpec private constructor(
    internal val mockServer: MockWebServer
) : MockWebServerBaseTest(mockServer) {

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
        id: String = TEST_MA_ID,
        version: String = TEST_MA_VERSION,
        name: String = TEST_MA_NAME,
        description: String = TEST_MA_DESCRIPTION,
        icon: String = TEST_MA_ICON,
        files: List<String> = listOf(TEST_URL_HTTPS_1, TEST_URL_HTTPS_2)
    ): MockResponse {
        val appInfo = hashMapOf(
            "id" to id,
            "versionId" to version,
            "name" to name,
            "description" to description,
            "icon" to icon,
            "files" to files
        )

        return MockResponse().setBody("[${Gson().toJson(appInfo)}]")
    }
}

class ListingApiRequestSpec : ListingApiSpec() {

    @Test
    fun `should fetch mini apps using the 'miniapps' endpoint`() {
        mockServer.enqueue(createResponse())

        retrofit.create(ListingApi::class.java)
            .list(hostAppVersion = TEST_MA_VERSION)
            .execute()

        mockServer.takeRequest().requestUrl!!.encodedPath shouldEndWith "miniapps"
    }

    @Test
    fun `should fetch mini apps for the 'android' platform`() {
        mockServer.enqueue(createResponse())

        retrofit.create(ListingApi::class.java)
            .list(hostAppVersion = TEST_MA_VERSION)
            .execute()

        mockServer.takeRequest().path!! shouldStartWith "/oneapp/android/"
    }

    @Test
    fun `should fetch mini apps for the provided host app version`() {
        mockServer.enqueue(createResponse())

        retrofit.create(ListingApi::class.java)
            .list(hostAppVersion = TEST_MA_VERSION)
            .execute()

        mockServer.takeRequest().path!! shouldContain "android/test_version/"
    }
}

class ListingApiResponseSpec : ListingApiSpec() {

    private lateinit var miniAppInfo: MiniAppInfo

    @Before
    fun setup() {
        mockServer.enqueue(createResponse())

        miniAppInfo = retrofit.create(ListingApi::class.java)
            .list(hostAppVersion = TEST_MA_VERSION)
            .execute().body()!![0]
    }

    @Test
    fun `should parse the 'id' from response`() {
        miniAppInfo.id shouldEqual TEST_MA_ID
    }

    @Test
    fun `should parse the 'version' from response`() {
        miniAppInfo.versionId shouldEqual TEST_MA_VERSION
    }

    @Test
    fun `should parse the 'name' from response`() {
        miniAppInfo.name shouldEqual TEST_MA_NAME
    }

    @Test
    fun `should parse the 'description' from response`() {
        miniAppInfo.description shouldEqual TEST_MA_DESCRIPTION
    }

    @Test
    fun `should parse the 'icon' from response`() {
        miniAppInfo.icon shouldEqual TEST_MA_ICON
    }

    @Test
    fun `should parse the 'files' from response`() {
        miniAppInfo.files shouldContain TEST_URL_HTTPS_1
        miniAppInfo.files shouldContain TEST_URL_HTTPS_2
    }
}
