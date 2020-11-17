package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.VisibleForTesting
import com.google.gson.GsonBuilder
import com.rakuten.tech.mobile.miniapp.BuildConfig
import com.rakuten.tech.mobile.sdkutils.RasSdkHeaders
import com.rakuten.tech.mobile.sdkutils.okhttp.addHeaderInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal fun createRetrofitClient(
    baseUrl: String,
    rasProjectId: String,
    subscriptionKey: String
) = createRetrofitClient(
    baseUrl = baseUrl,
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
    headers: RasSdkHeaders
): Retrofit {
    @Suppress("SpreadOperator")
    val httpClient = OkHttpClient.Builder()
        .addHeaderInterceptor(*headers.asArray())
        .addInterceptor(provideHeaderInterceptor())
        .build()
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
