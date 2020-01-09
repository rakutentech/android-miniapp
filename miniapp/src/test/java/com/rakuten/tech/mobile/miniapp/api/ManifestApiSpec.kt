package com.rakuten.tech.mobile.miniapp.api

import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEndWith
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

open class ManifestApiSpec private constructor(
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
        files: List<String> = listOf("https://www.example.com/1", "https://www.example.com/2")
    ): MockResponse {
        val manifestInfo = hashMapOf(
            "files" to files
        )

        return MockResponse().setBody(Gson().toJson(manifestInfo))
    }
}

class ManifestApiRequestSpec : ManifestApiSpec() {

    @Test
    fun `should fetch files information of a mini app using the 'manifest' endpoint`() {
        mockServer.enqueue(createResponse())

        val miniappId = UUID.randomUUID()
        val versionId = UUID.randomUUID()

        retrofit.create(ManifestApi::class.java)
            .fetchFileListFromManifest(miniappId, versionId)
            .execute()

        mockServer.takeRequest().requestUrl!!.encodedPath shouldEndWith "manifest"
    }

    @Test
    fun `should fetch files information of a specific mini app version`() {
        mockServer.enqueue(createResponse())

        val miniappId = UUID.randomUUID()
        val versionId = UUID.randomUUID()

        retrofit.create(ManifestApi::class.java)
            .fetchFileListFromManifest(miniappId, versionId)
            .execute()

        mockServer.takeRequest().path!! shouldContain "miniapp/$miniappId/version/$versionId/"
    }
}

class ManifestApiResponseSpec : ManifestApiSpec() {

    private lateinit var manifestEntity: ManifestEntity

    @Before
    fun setup() {
        mockServer.enqueue(createResponse())

        val miniappId = UUID.randomUUID()
        val versionId = UUID.randomUUID()

        manifestEntity = retrofit.create(ManifestApi::class.java)
            .fetchFileListFromManifest(miniappId, versionId)
            .execute().body()!!
    }

    @Test
    fun `should parse the 'files' from response`() {
        manifestEntity.files shouldContain "https://www.example.com/1"
        manifestEntity.files shouldContain "https://www.example.com/2"
    }
}
