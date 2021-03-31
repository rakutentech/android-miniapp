package com.rakuten.tech.mobile.miniapp.api

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.miniapp.*
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.http.Url
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.pow

internal class ApiClient @VisibleForTesting constructor(
    retrofit: Retrofit,
    internal val isPreviewMode: Boolean,
    private val hostId: String,
    private val appInfoApi: AppInfoApi = retrofit.create(AppInfoApi::class.java),
    private val downloadApi: DownloadApi = retrofit.create(DownloadApi::class.java),
    private val manifestApi: ManifestApi = retrofit.create(ManifestApi::class.java),
    private val metadataApi: MetadataApi = retrofit.create(MetadataApi::class.java),
    private val requestExecutor: RetrofitRequestExecutor = RetrofitRequestExecutor(retrofit)
) {

    constructor(
        baseUrl: String,
        rasProjectId: String,
        subscriptionKey: String,
        isPreviewMode: Boolean = false
    ) : this(
        retrofit = createRetrofitClient(
            baseUrl = baseUrl,
            rasProjectId = rasProjectId,
            subscriptionKey = subscriptionKey
        ),
        isPreviewMode = isPreviewMode,
        hostId = rasProjectId
    )

    private val testPath = if (isPreviewMode) "preview" else ""

    @Throws(MiniAppSdkException::class)
    suspend fun list(): List<MiniAppInfo> {
        val request = appInfoApi.list(
            hostId = hostId,
            testPath = testPath
        )
        return requestExecutor.executeRequest(request)
    }

    @Throws(MiniAppSdkException::class)
    suspend fun fetchInfo(appId: String): MiniAppInfo {
        val request = appInfoApi.fetchInfo(
            hostId = hostId,
            miniAppId = appId,
            testPath = testPath
        )
        val info = requestExecutor.executeRequest(request)

        if (info.isNotEmpty()) {
            return info.first()
        } else {
            throw MiniAppHasNoPublishedVersionException(appId)
        }
    }

    @Throws(MiniAppSdkException::class)
    suspend fun fetchFileList(miniAppId: String, versionId: String): ManifestEntity {
        val request = manifestApi.fetchFileListFromManifest(
            hostId = hostId,
            miniAppId = miniAppId,
            versionId = versionId,
            testPath = testPath
        )
        return requestExecutor.executeRequest(request)
    }

    @Throws(MiniAppSdkException::class)
    suspend fun fetchMiniAppManifest(miniAppId: String, versionId: String): MetadataEntity {
        val request = metadataApi.fetchMetadata(
            hostId = hostId,
            miniAppId = miniAppId,
            versionId = versionId,
            testPath = testPath
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

    private var retryCount = 0

    private inline fun <reified T : ErrorResponse> createErrorConverter(retrofit: Retrofit) =
        retrofit.responseBodyConverter<T>(T::class.java, arrayOfNulls<Annotation>(0))

    @Suppress("TooGenericExceptionCaught", "ThrowsCount")
    suspend fun <T> executeRequest(call: Call<T>): T = try {
        val response = call.execute()

        // retry network request when there is 500 error code from the server
        if (response.code() >= 500) {
            if (retryCount++ < TOTAL_RETRIES) {
                Log.v(MINIAPP_NETWORK_RETRY, "Retrying in $retryCount out of $TOTAL_RETRIES times.")

                // setting retrying policy about waiting time
                val backOff = 2.0
                val waitTime = 0.5 * backOff.pow(retryCount.toDouble())
                try {
                    Thread.sleep(waitTime.toLong())
                } catch (interruptedException: InterruptedException) {
                    Log.e(MINIAPP_NETWORK_RETRY, "intercept: ", interruptedException)
                }
                // recall the request
                executeRequest(call.clone())
            }
        }

        when {
            response.isSuccessful -> {
                // Body shouldn't be null if request was successful
                response.body() ?: throw sdkExceptionForInternalServerError()
            }
            else -> throw exceptionForHttpError<T>(response)
        }
    } catch (error: Exception) {
        when (error) {
            is UnknownHostException,
            is SocketTimeoutException -> throw MiniAppNetException(error)
            is MiniAppSdkException -> throw error
            else -> throw MiniAppSdkException(error) // when response is not Type T or malformed JSON is received
        }
    }

    @Throws(MiniAppSdkException::class, MiniAppNotFoundException::class)
    @Suppress("MagicNumber", "ThrowsCount")
    private fun <T> exceptionForHttpError(response: Response<T>): MiniAppSdkException {
        // Error body shouldn't be null if request wasn't successful
        val errorData = response.errorBody() ?: throw sdkExceptionForInternalServerError()
        when (response.code()) {
            401, 403 -> throw MiniAppSdkException(
                convertAuthErrorToMsg(
                    response, errorData, createErrorConverter(retrofit)
                )
            )
            404 -> throw MiniAppNotFoundException(response.message())
            else -> throw MiniAppSdkException(
                convertStandardHttpErrorToMsg(
                    response, errorData, createErrorConverter(retrofit)
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
    @SerializedName("code") val code: Int,
    @SerializedName("message") override val message: String
) : ErrorResponse

internal data class AuthErrorResponse(
    @SerializedName("code") val code: String,
    @SerializedName("message") override val message: String
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

private const val TOTAL_RETRIES = 5
private const val MINIAPP_NETWORK_RETRY = "MINIAPP_NETWORK_RETRY"
