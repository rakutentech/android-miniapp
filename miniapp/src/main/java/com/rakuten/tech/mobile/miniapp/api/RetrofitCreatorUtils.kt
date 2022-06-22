package com.rakuten.tech.mobile.miniapp.api

import android.os.Build
import androidx.annotation.VisibleForTesting
import com.google.gson.GsonBuilder
import com.rakuten.tech.mobile.miniapp.BuildConfig
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalArgumentException
import java.net.MalformedURLException
import java.net.URI

internal fun createRetrofitClient(
    baseUrl: String,
    pubKeyList: List<String>,
    rasProjectId: String,
    subscriptionKey: String
) = createRetrofitClient(
    baseUrl = baseUrl,
    pubKeyList = pubKeyList,
    headers = RasSdkHeaders(
        appId = "ras-app-id" to rasProjectId,
        subscriptionKey = "apiKey" to "ras-$subscriptionKey",
        sdkName = "ras-sdk-name" to "MiniApp",
        sdkVersion = "ras-sdk-version" to BuildConfig.VERSION_NAME,
        deviceModel = "ras-device-model" to Build.MODEL,
        deviceOs = "ras-os-version" to Build.VERSION.RELEASE
    )
)

@VisibleForTesting
internal fun createRetrofitClient(
    baseUrl: String,
    pubKeyList: List<String>,
    headers: RasSdkHeaders
): Retrofit {
    @Suppress("SpreadOperator")
    val httpClientBuilder = OkHttpClient.Builder()
        .addHeaderInterceptor(*headers.asArray())
        .addInterceptor(provideHeaderInterceptor())
    if (pubKeyList.isNotEmpty()) {
        httpClientBuilder.certificatePinner(
            createCertificatePinner(
                baseUrl = baseUrl,
                pubKeyList = pubKeyList
            )
        )
    }
    val httpClient = httpClientBuilder.build()

    return Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .baseUrl(baseUrl)
        .client(httpClient)
        .build()
}

private fun provideHeaderInterceptor(): Interceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        .header("Accept-Encoding", "identity")
        .build()

    chain.proceed(request)
}

@VisibleForTesting
internal fun createCertificatePinner(baseUrl: String, pubKeyList: List<String>): CertificatePinner {
    val certificatePinnerBuilder = CertificatePinner.Builder()
    for (pubKey in pubKeyList) {
        certificatePinnerBuilder.add(extractBaseUrl(baseUrl), pubKey)
    }
    return certificatePinnerBuilder.build()
}

@VisibleForTesting
@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal fun extractBaseUrl(baseUrl: String): String {
    return try {
        val url = URI.create(baseUrl).toURL()
        url.authority
    } catch (e: IllegalArgumentException) {
        ""
    } catch (e: MalformedURLException) {
        ""
    }
}

/**
 * Adds an Interceptor to OkHttp's okhttp3.OkHttpClient which will add the provided headers to all requests.
 * @param headers the headers to be added to all requests.
 */
private fun OkHttpClient.Builder.addHeaderInterceptor(
    vararg headers: Pair<String, String>
): OkHttpClient.Builder = this.addNetworkInterceptor(HeaderInterceptor(headers))

private class HeaderInterceptor constructor(
    private val headers: Array<out Pair<String, String>>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        headers.forEach { header ->
            requestBuilder.addHeader(header.first, header.second)
        }

        return chain.proceed(
            requestBuilder.build()
        )
    }
}

/**
 * Standard headers that should be sent with all requests to RAS.
 */
internal class RasSdkHeaders(
    private val appId: Pair<String, String>,
    private val subscriptionKey: Pair<String, String>,
    private val sdkName: Pair<String, String>,
    private val sdkVersion: Pair<String, String>,
    private val deviceModel: Pair<String, String>,
    private val deviceOs: Pair<String, String>
) {

    /**
     * Returns the RAS headers as an array of [Pair]'s.
     *
     * @return array of [Pair] objects with [Pair.first] holding the header name
     * and [Pair.second] holding the header value.
     */
    fun asArray() = arrayOf(
        subscriptionKey,
        appId,
        sdkName,
        sdkVersion,
        deviceModel,
        deviceOs
    )
}
