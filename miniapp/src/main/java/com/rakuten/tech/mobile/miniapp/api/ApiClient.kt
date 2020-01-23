package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Url

internal class ApiClient @VisibleForTesting constructor(
    retrofit: Retrofit,
    private val hostAppVersion: String,
    private val requestExecutor: RetrofitRequestExecutor = RetrofitRequestExecutor(retrofit),
    private val listingApi: ListingApi = retrofit.create(ListingApi::class.java),
    private val manifestApi: ManifestApi = retrofit.create(ManifestApi::class.java),
    private val downloadApi: DownloadApi = retrofit.create(DownloadApi::class.java)
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

    suspend fun list(): List<MiniAppInfo> {
        val request = listingApi.list(hostAppVersion = hostAppVersion)
        return requestExecutor.executeRequest(request)
    }

    suspend fun fetchFileList(miniAppId: String, versionId: String): ManifestEntity {
        val request = manifestApi.fetchFileListFromManifest(miniAppId, versionId)
        return requestExecutor.executeRequest(request)
    }

    suspend fun downloadFile(@Url url: String): ResponseBody {
        val request = downloadApi.downloadFile(url)
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

    @Suppress("TooGenericExceptionCaught")
    suspend fun <T> executeRequest(call: Call<T>): T {
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                return response.body()!! // Body can't be null if request was successful
            } else {
                val error =
                    response.errorBody()!! // Error body can't be null if request wasn't successful
                throw sdkExceptionFromHttpException(response, error)
            }
        } catch (error: Exception) { // hotfix to catch the case when response is not Type T
            throw MiniAppSdkException(error)
        }
    }

    private fun sdkExceptionFromHttpException(
        response: Response<in Nothing>,
        error: ResponseBody
    ): MiniAppSdkException {
        return MiniAppSdkException(
            MiniAppHttpException(
                response = response,
                errorMessage = errorConvertor.convert(error)?.message
                    ?: "No error message provided by server."
            ).errorMessage
        )
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
internal class MiniAppHttpException(
    response: Response<in Nothing>,
    val errorMessage: String
) : HttpException(response) {

    /**
     * Readable message of error response in the format
     * "HTTP {CODE} {STATUS MESSAGE}: {ERROR MESSAGE}".
     */
    override fun message() = "${super.message()}: $errorMessage"
}
