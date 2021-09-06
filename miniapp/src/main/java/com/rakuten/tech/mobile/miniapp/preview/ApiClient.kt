package com.rakuten.tech.mobile.miniapp.preview

import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.api.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal class ApiClient constructor(
    retrofit: Retrofit,
    private val hostId: String,
    private val previewAppInfoApi: PreviewAppInfoApi = retrofit.create(PreviewAppInfoApi::class.java),
    private val requestExecutor: RetrofitRequestExecutor = RetrofitRequestExecutor(retrofit)
) {

    constructor(
        baseUrl: String,
        pubKey: String,
        rasProjectId: String,
        subscriptionKey: String
    ) : this(
        retrofit = createRetrofitClientWithCertPinner(
            baseUrl = baseUrl,
            pubKey = pubKey,
            rasProjectId = rasProjectId,
            subscriptionKey = subscriptionKey
        ),
        hostId = rasProjectId
    )

    @Throws(MiniAppSdkException::class)
    suspend fun fetchInfoByPreviewCode(previewCode: String): MiniAppInfo {
        val request = previewAppInfoApi.fetchInfoByPreviewCode(
            hostId = hostId,
            previewCode = previewCode
        )
        val info = requestExecutor.executeRequest(request)

        if (info.miniapp != null) {
            return info.miniapp
        } else {
            throw MiniAppSdkException("")
        }
    }
}


internal class RetrofitRequestExecutor(private val retrofit: Retrofit) {

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

    @Suppress("TooGenericExceptionCaught", "ThrowsCount")
    suspend fun <T> executeRequest(call: Call<T>): T = try {
        val response = call.execute()

        when {
            response.isSuccessful -> {
                // Body shouldn't be null if request was successful
                response.body() ?: throw sdkExceptionForInternalServerError()
            }
            else -> throw exceptionForHttpError(response)
        }
    } catch (error: Exception) {
        when (error) {
            is UnknownHostException,
            is SocketTimeoutException -> throw MiniAppNetException(error)
            is MiniAppSdkException -> throw error
            else -> throw MiniAppSdkException(error) // when response is not Type T or malformed JSON is received
        }
    }

    private inline fun <reified T : ErrorResponse> createErrorConverter(retrofit: Retrofit) =
        retrofit.responseBodyConverter<T>(T::class.java, arrayOfNulls<Annotation>(0))

    private fun <T> exceptionForHttpError(response: Response<T>): MiniAppSdkException {
        // Error body shouldn't be null if request wasn't successful
        val errorData = response.errorBody() ?: throw sdkExceptionForInternalServerError()
        when (response.code()) {
            401, 403 -> throw MiniAppSdkException(
                convertAuthErrorToMsg(
                    response, errorData, createErrorConverter(retrofit)
                )
            )
            400 -> throw MiniAppHostException(response.message())
            404 -> throw MiniAppNotFoundException(response.message())
            else -> throw MiniAppSdkException(
                convertStandardHttpErrorToMsg(
                    response, errorData, createErrorConverter(retrofit)
                )
            )
        }
    }
}
