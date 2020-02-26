package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.sdkExceptionForInternalServerError
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Url

internal class ApiClient @VisibleForTesting constructor(
    retrofit: Retrofit,
    private val hostAppVersionId: String,
    private val hostAppId: String,
    private val appInfoApi: AppInfoApi = retrofit.create(AppInfoApi::class.java),
    private val downloadApi: DownloadApi = retrofit.create(DownloadApi::class.java),
    private val manifestApi: ManifestApi = retrofit.create(ManifestApi::class.java),
    private val requestExecutor: RetrofitRequestExecutor = RetrofitRequestExecutor(retrofit)
) {

    constructor(
        baseUrl: String,
        rasAppId: String,
        subscriptionKey: String,
        hostAppVersionId: String
    ) : this(
        retrofit = createRetrofitClient(
            baseUrl = baseUrl,
            rasAppId = rasAppId,
            subscriptionKey = subscriptionKey
        ),
        hostAppVersionId = hostAppVersionId,
        hostAppId = rasAppId
    )

    suspend fun list(): List<MiniAppInfo> {
        val request = appInfoApi.list(hostAppId, hostAppVersionId)
        return requestExecutor.executeRequest(request)
    }

    suspend fun fetchInfo(appId: String): MiniAppInfo {
        val request = appInfoApi.fetchInfo(hostAppId, hostAppVersionId, appId)
        return requestExecutor.executeRequest(request).first()
    }

    suspend fun fetchFileList(miniAppId: String, versionId: String): ManifestEntity {
        val request = manifestApi.fetchFileListFromManifest(
            hostAppId = hostAppId,
            miniAppId = miniAppId,
            versionId = versionId,
            hostAppVersionId = hostAppVersionId
        )
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
            when {
                response.isSuccessful -> {
                    // Body shouldn't be null if request was successful
                    return response.body() ?: throw sdkExceptionForInternalServerError()
                }
                else -> {
                    // Error body shouldn't be null if request wasn't successful
                    val error = response.errorBody() ?: throw sdkExceptionForInternalServerError()
                    throw sdkExceptionFromHttpException(response, error)
                }
            }
        } catch (error: Exception) { // when response is not Type T or malformed JSON is received
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
