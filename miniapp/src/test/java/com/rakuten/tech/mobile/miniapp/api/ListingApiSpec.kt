package com.rakuten.tech.mobile.miniapp.api

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.*
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
        id: String = "test_id",
        version: String = "test_version",
        name: String = "test_name",
        description: String = "test_description",
        icon: String = "test_icon",
        files: List<String> = listOf("https://www.example.com/1", "https://www.example.com/2")
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
            .list(hostAppVersion = "test_version")
            .execute()

        mockServer.takeRequest().requestUrl!!.encodedPath shouldEndWith "miniapps"
    }

    @Test
    fun `should fetch mini apps for the 'android' platform`() {
        mockServer.enqueue(createResponse())

        retrofit.create(ListingApi::class.java)
            .list(hostAppVersion = "test_version")
            .execute()

        mockServer.takeRequest().path!! shouldStartWith "/oneapp/android/"
    }

    @Test
    fun `should fetch mini apps for the provided host app version`() {
        mockServer.enqueue(createResponse())

        retrofit.create(ListingApi::class.java)
            .list(hostAppVersion = "test_version")
            .execute()

        mockServer.takeRequest().path!! shouldContain "android/test_version/"
    }
}

class ListingApiResponseSpec : ListingApiSpec() {

    private lateinit var miniAppInfo: MiniAppInfo

    @Before
    fun setup() {
        mockServer.enqueue(createResponse(
            id = "test_id",
            version = "test_version",
            name = "test_name",
            description = "test_description",
            icon = "test_icon",
            files = listOf("https://www.example.com/1", "https://www.example.com/2")
        ))

        miniAppInfo = retrofit.create(ListingApi::class.java)
            .list(hostAppVersion = "test_version")
            .execute().body()!![0]
    }

    @Test
    fun `should parse the 'id' from response`() {
        miniAppInfo.id shouldEqual "test_id"
    }

    @Test
    fun `should parse the 'version' from response`() {
        miniAppInfo.versionId shouldEqual "test_version"
    }

    @Test
    fun `should parse the 'name' from response`() {
        miniAppInfo.name shouldEqual "test_name"
    }

    @Test
    fun `should parse the 'description' from response`() {
        miniAppInfo.description shouldEqual "test_description"
    }

    @Test
    fun `should parse the 'icon' from response`() {
        miniAppInfo.icon shouldEqual "test_icon"
    }

    @Test
    fun `should parse the 'files' from response`() {
        miniAppInfo.files shouldContain "https://www.example.com/1"
        miniAppInfo.files shouldContain "https://www.example.com/2"
    }
}
