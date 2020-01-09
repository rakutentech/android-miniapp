package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.VisibleForTesting
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.util.UUID

internal class ApiClient @VisibleForTesting constructor(
    retrofit: Retrofit,
    private val hostAppVersion: String,
    private val requestExecutor: RetrofitRequestExecutor = RetrofitRequestExecutor(retrofit),
    private val listingApi: ListingApi = retrofit.create(ListingApi::class.java),
    private val manifestApi: ManifestApi = retrofit.create(ManifestApi::class.java)
) {

    constructor(
        baseUrl: String,
        rasAppId: String,
        subscriptionKey: String,
        hostAppVersion: String
    ) : this(
        retrofit = createRetrofitClient(
            baseUrl = baseUrl,
            rasAppId = rasAppId,
            subscriptionKey = subscriptionKey
        ),
        hostAppVersion = hostAppVersion
    )

    suspend fun list(): List<ListingEntity> {
        val request = listingApi.list(hostAppVersion = hostAppVersion)
        return requestExecutor.executeRequest(request)
    }

    suspend fun fetchFileList(miniAppId: UUID, versionId: UUID): ManifestEntity {
        val request = manifestApi.fetchFileListFromManifest(miniAppId, versionId)
        return requestExecutor.executeRequest(request)
    }
}

internal class RetrofitRequestExecutor(
    retrofit: Retrofit
) {

    private val errorConvertor = retrofit.responseBodyConverter<ErrorResponse>(
        ErrorResponse::class.java,
        arrayOfNulls<Annotation>(0)
    )

    suspend fun <T> executeRequest(call: Call<T>): T {
        val response = call.execute()

        if (response.isSuccessful) {
            return response.body()!! // Body can't be null if request was successful
        } else {
            val error = response.errorBody()!! // Error body can't be null if request wasn't successful
            throw MiniAppHttpException(
                response = response,
                errorMessage = errorConvertor.convert(error)?.message
                    ?: "No error message provided by server."
            )
        }
    }
}

internal data class ErrorResponse(
    val code: Int,
    val message: String
)

/**
 * Exception thrown when the Mini App API returns an error response.
 * @param response Response from the server
 * @param errorMessage Error message returned by the server
 */
class MiniAppHttpException(
    response: Response<in Nothing>,
    val errorMessage: String
) : HttpException(response) {

    /**
     * Readable message of error response in the format
     * "HTTP {CODE} {STATUS MESSAGE}: {ERROR MESSAGE}".
     */
    override fun message() = "${super.message()}: $errorMessage"
}
