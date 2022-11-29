package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.iap.MiniAppPurchaseRequest
import com.rakuten.tech.mobile.miniapp.iap.MiniAppPurchaseResponse
import com.rakuten.tech.mobile.miniapp.iap.PurchaseItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface InAppPurchaseApi {
    @GET("host/{hostId}/miniapp/{miniappId}/purchase-items")
    suspend fun getPurchaseItems(
        @Path("hostId") hostId: String,
        @Path("miniappId") miniAppId: String,
    ): Call<List<PurchaseItem>>

    @POST("host/{hostId}/miniapp/{miniappId}/transaction")
    suspend fun purchaseItem(
        @Path("hostId") hostId: String,
        @Path("miniappId") miniAppId: String,
        @Body request: MiniAppPurchaseRequest
    ): Call<MiniAppPurchaseResponse>

    @GET("host/{hostId}/miniapp/{miniappId}/transaction/{transactionToken}")
    suspend fun getTransactionStatus(
        @Path("hostId") hostId: String,
        @Path("miniappId") miniAppId: String,
        @Path("transactionToken") transactionToken: String
    ): Call<MiniAppPurchaseResponse>
}
