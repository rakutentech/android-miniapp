package com.rakuten.tech.mobile.miniapp.api

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEndWith
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class ManifestApiSpec private constructor(
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
        files: List<String> = listOf(TEST_URL_HTTPS_1, TEST_URL_HTTPS_2)
    ): MockResponse = MockResponse().setBody(
        Gson().toJson(
            hashMapOf(
                "manifest" to files
            )
        )
    )
}

class ManifestApiRequestSpec : ManifestApiSpec() {

    @Test
    fun `should fetch files information of a mini app using the 'manifest' endpoint`() {
        executeManifestCallByRetrofit()
        val requestUrl = mockServer.takeRequest().requestUrl!!
        requestUrl.encodedPath shouldEndWith "manifest"
        requestUrl.encodedQuery.toString() shouldContain "hostVersion=$TEST_HA_ID_VERSION"
    }

    @Test
    fun `should fetch files information of a specific mini app version`() {
        executeManifestCallByRetrofit()
        mockServer.takeRequest().path!! shouldContain
                "miniapp/$TEST_ID_MINIAPP/version/$TEST_ID_MINIAPP_VERSION/"
    }

    @Test
    fun `should have test endpoint when in test mode`() {
        mockServer.enqueue(createResponse())
        retrofit.create(ManifestApi::class.java)
            .fetchFileListFromManifest(
                hostAppId = TEST_HA_ID_APP,
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION,
                hostAppVersionId = TEST_HA_ID_VERSION,
                testPath = "test"
            ).execute()
        mockServer.takeRequest().path!! shouldContain "test"
    }

    private fun executeManifestCallByRetrofit() {
        mockServer.enqueue(createResponse())
        retrofit.create(ManifestApi::class.java)
            .fetchFileListFromManifest(
                hostAppId = TEST_HA_ID_APP,
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION,
                hostAppVersionId = TEST_HA_ID_VERSION
            ).execute()
    }
}

class ManifestApiResponseSpec : ManifestApiSpec() {

    private lateinit var manifestEntity: ManifestEntity

    @Before
    fun setup() {
        mockServer.enqueue(createResponse())
        manifestEntity = retrofit.create(ManifestApi::class.java)
            .fetchFileListFromManifest(
                hostAppId = TEST_HA_ID_APP,
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION,
                hostAppVersionId = TEST_HA_ID_VERSION
            )
            .execute().body()!!
    }

    @Test
    fun `should parse the 'files' from response`() {
        manifestEntity.files shouldContain TEST_URL_HTTPS_1
        manifestEntity.files shouldContain TEST_URL_HTTPS_2
    }
}
