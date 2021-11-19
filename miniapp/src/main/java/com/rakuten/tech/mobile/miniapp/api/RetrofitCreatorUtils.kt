package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.VisibleForTesting
import com.google.gson.GsonBuilder
import com.rakuten.tech.mobile.miniapp.BuildConfig
import com.rakuten.tech.mobile.sdkutils.RasSdkHeaders
import com.rakuten.tech.mobile.sdkutils.okhttp.addHeaderInterceptor
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalArgumentException
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
        appId = rasProjectId,
        subscriptionKey = subscriptionKey,
        sdkName = "MiniApp",
        sdkVersion = BuildConfig.VERSION_NAME
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
internal fun extractBaseUrl(baseUrl: String): String {
    return try {
        val url = URI.create(baseUrl).toURL()
        url.authority
    } catch (e: IllegalArgumentException) {
        ""
    }
}
