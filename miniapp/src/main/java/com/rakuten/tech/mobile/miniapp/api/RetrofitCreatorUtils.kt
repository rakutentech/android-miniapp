package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.VisibleForTesting
import com.google.gson.GsonBuilder
import com.rakuten.tech.mobile.miniapp.BuildConfig
import com.rakuten.tech.mobile.sdkutils.RasSdkHeaders
import com.rakuten.tech.mobile.sdkutils.okhttp.addHeaderInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal fun createRetrofitClient(
    baseUrl: String,
    rasAppId: String,
    subscriptionKey: String
) = createRetrofitClient(
    baseUrl = baseUrl,
    headers = RasSdkHeaders(
        appId = rasAppId,
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
        .build()
    return Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().setLenient().create()
            )
        )
        .baseUrl(baseUrl)
        .client(httpClient)
        .build()
}
