package com.rakuten.mobile.miniapp.download.network.client

import com.rakuten.mobile.miniapp.platform.BuildConfig
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Retrofit object wrapper.
 */
@Singleton
class RetrofitClient {

    val retrofit: Retrofit

    init {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(TIME_OUT_IN_SECONDS, TimeUnit.SECONDS)
            .callTimeout(TIME_OUT_IN_SECONDS, TimeUnit.SECONDS)
            .connectionPool(
                ConnectionPool(
                    MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION_IN_SECONDS, TimeUnit.SECONDS
                )
            )
            .build()

        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.MINI_APP_BASE_URL)
            .client(okHttpClient)
            .build()
    }

    companion object {
        private const val TIME_OUT_IN_SECONDS = 15L
        private const val MAX_IDLE_CONNECTIONS = 5
        private const val KEEP_ALIVE_DURATION_IN_SECONDS = 60L
    }
}
