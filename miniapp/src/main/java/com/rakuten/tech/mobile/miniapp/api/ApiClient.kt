package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.sdkExceptionForInternalServerError
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
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
    private val retrofit: Retrofit
) {

    private inline fun <reified T : ErrorResponse> createErrorConvertor(retrofit: Retrofit) =
        retrofit.responseBodyConverter<T>(T::class.java, arrayOfNulls<Annotation>(0))

    @Suppress("TooGenericExceptionCaught", "ThrowsCount")
    suspend fun <T> executeRequest(call: Call<T>): T = try {
        val response = call.execute()
        when {
            response.isSuccessful -> {
                // Body shouldn't be null if request was successful
                response.body() ?: throw sdkExceptionForInternalServerError()
            }
            else -> throw exceptionForHttpError<T>(response)
        }
    } catch (error: Exception) { // when response is not Type T or malformed JSON is received
        throw MiniAppSdkException(error)
    }

    @Throws(MiniAppSdkException::class)
    @Suppress("MagicNumber", "ThrowsCount")
    private fun <T> exceptionForHttpError(response: Response<T>): MiniAppSdkException {
        // Error body shouldn't be null if request wasn't successful
        val errorData = response.errorBody() ?: throw sdkExceptionForInternalServerError()
        when (response.code()) {
            401, 403 -> throw MiniAppSdkException(
                convertAuthErrorToMsg(
                    response, errorData, createErrorConvertor(retrofit)
                )
            )
            else -> throw MiniAppSdkException(
                convertStandardHttpErrorToMsg(
                    response, errorData, createErrorConvertor(retrofit)
                )
            )
        }
    }

    private fun convertAuthErrorToMsg(
        response: Response<in Nothing>,
        error: ResponseBody,
        converter: Converter<ResponseBody, AuthErrorResponse>
    ) = errorMsgFromHttpException(response, converter.convert(error)?.message)

    private fun convertStandardHttpErrorToMsg(
        response: Response<in Nothing>,
        error: ResponseBody,
        converter: Converter<ResponseBody, HttpErrorResponse>
    ) = errorMsgFromHttpException(response, converter.convert(error)?.message)

    private fun errorMsgFromHttpException(
        response: Response<in Nothing>,
        error: String?
    ) = MiniAppHttpException(
        response = response,
        errorMessage = error ?: "No error message provided by server."
    ).message()
}

internal data class HttpErrorResponse(
    val code: Int,
    override val message: String
) : ErrorResponse

internal data class AuthErrorResponse(
    val code: String,
    override val message: String
) : ErrorResponse

internal interface ErrorResponse {
    val message: String
}

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
