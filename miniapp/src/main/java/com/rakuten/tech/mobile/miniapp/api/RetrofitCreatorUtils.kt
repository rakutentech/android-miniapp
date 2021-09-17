package com.rakuten.tech.mobile.miniapp.api

import android.util.Patterns
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
import java.net.MalformedURLException
import java.net.URI

internal fun createRetrofitClient(
    baseUrl: String,
    pubKey: String,
    rasProjectId: String,
    subscriptionKey: String
) = createRetrofitClient(
    baseUrl = baseUrl,
    pubKey = pubKey,
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
    pubKey: String,
    headers: RasSdkHeaders
): Retrofit {
    @Suppress("SpreadOperator")
    val httpClientBuilder = OkHttpClient.Builder()
        .addHeaderInterceptor(*headers.asArray())
        .addInterceptor(provideHeaderInterceptor())
    if (pubKey != "") {
        httpClientBuilder.certificatePinner(
            createCertificatePinner(
                baseUrl = baseUrl,
                pubKey = pubKey
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

private fun createCertificatePinner(baseUrl: String, pubKey: String): CertificatePinner {
    return CertificatePinner.Builder()
        .add(baseUrl.extractAuthority(), pubKey)
        .build()
}

fun String.extractAuthority(): String {
    return try {
        URI.create(this).toURL().authority
    } catch (e: MalformedURLException) {
        ""
    }
}
