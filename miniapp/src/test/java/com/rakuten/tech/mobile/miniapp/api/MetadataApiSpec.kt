package com.rakuten.tech.mobile.miniapp.api

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEndWith
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class MetadataApiSpec private constructor(
    internal val mockServer: MockWebServer
) : MockWebServerBaseSpec(mockServer) {

    constructor() : this(MockWebServer())

    private lateinit var baseUrl: String
    internal lateinit var retrofit: Retrofit

    private val requiredPermissionObj =
        listOf(MetadataPermissionObj("rakuten.miniapp.user.USER_NAME", "reason"))
    private val optionalPermissionObj =
        listOf(MetadataPermissionObj("rakuten.miniapp.user.PROFILE_PHOTO", "reason"))
    internal val metadataResponse = MetadataResponse(
            requiredPermissionObj, optionalPermissionObj, TEST_ATP_LIST, hashMapOf()
    )

    @Before
    fun baseSetup() {
        baseUrl = mockServer.url("/").toString()
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    internal fun createResponse(
        metadata: MetadataResponse = metadataResponse
    ): MockResponse = MockResponse().setBody(
        Gson().toJson(
            hashMapOf(
                "bundleManifest" to metadata
            )
        )
    )
}

class MetadataApiRequestSpec : MetadataApiSpec() {

    @Test
    fun `should fetch metadata of a mini app using the 'metadata' endpoint`() {
        executeMetadataCallByRetrofit()
        val requestUrl = mockServer.takeRequest().requestUrl!!
        requestUrl.encodedPath shouldEndWith "metadata"
    }

    @Test
    fun `should fetch metadata of a specific mini app version`() {
        executeMetadataCallByRetrofit()
        mockServer.takeRequest().path!! shouldContain
                "miniapp/$TEST_ID_MINIAPP/version/$TEST_ID_MINIAPP_VERSION/"
    }

    @Test
    fun `should have test endpoint when in test mode`() {
        mockServer.enqueue(createResponse())
        retrofit.create(MetadataApi::class.java)
            .fetchMetadataPreviewMode(
                hostId = TEST_HA_ID_PROJECT,
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION,
                testPath = "test",
                lang = TEST_MA_LANGUAGE_CODE
            ).execute()
        mockServer.takeRequest().path!! shouldContain "test"
    }

    @Test
    fun `should fetch metadata of a mini app using the 'lang' query`() {
        executeMetadataCallByRetrofit()
        val requestUrl = mockServer.takeRequest().requestUrl!!
        requestUrl.encodedQuery shouldBeEqualTo "lang=$TEST_MA_LANGUAGE_CODE"
    }

    private fun executeMetadataCallByRetrofit() {
        mockServer.enqueue(createResponse())
        retrofit.create(MetadataApi::class.java)
            .fetchMetadata(
                hostId = TEST_HA_ID_PROJECT,
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION,
                lang = TEST_MA_LANGUAGE_CODE
            ).execute()
    }
}

class MetadataApiResponseSpec : MetadataApiSpec() {

    private lateinit var metadataEntity: MetadataEntity

    @Before
    fun setup() {
        mockServer.enqueue(createResponse())
        metadataEntity = retrofit.create(MetadataApi::class.java)
            .fetchMetadata(
                hostId = TEST_HA_ID_PROJECT,
                miniAppId = TEST_ID_MINIAPP,
                versionId = TEST_ID_MINIAPP_VERSION,
                lang = TEST_MA_LANGUAGE_CODE
            )
            .execute().body()!!
    }

    @Test
    fun `should parse the metadata from response`() {
        metadataEntity.metadata shouldBeEqualTo metadataResponse
    }
}
