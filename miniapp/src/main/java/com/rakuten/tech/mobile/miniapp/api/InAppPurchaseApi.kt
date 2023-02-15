package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.iap.MiniAppPurchaseItemListResponse
import com.rakuten.tech.mobile.miniapp.iap.MiniAppPurchaseRequest
import com.rakuten.tech.mobile.miniapp.iap.MiniAppPurchaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface InAppPurchaseApi {
    @GET("host/{hostId}/miniapp/{miniappId}/purchase-items")
    fun getPurchaseItems(
        @Path("hostId") hostId: String,
        @Path("miniappId") miniAppId: String,
    ): Call<MiniAppPurchaseItemListResponse>

    @POST("host/{hostId}/miniapp/{miniappId}/transaction")
    fun purchaseItem(
        @Path("hostId") hostId: String,
        @Path("miniappId") miniAppId: String,
        @Body request: MiniAppPurchaseRequest
    ): Call<MiniAppPurchaseResponse>

    @GET("host/{hostId}/miniapp/{miniappId}/transaction/{transactionToken}")
    fun getTransactionStatus(
        @Path("hostId") hostId: String,
        @Path("miniappId") miniAppId: String,
        @Path("transactionToken") transactionToken: String
    ): Call<MiniAppPurchaseResponse>
}
