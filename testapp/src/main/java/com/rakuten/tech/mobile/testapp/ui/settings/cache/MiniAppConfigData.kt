package com.rakuten.tech.mobile.testapp.ui.settings.cache

import com.google.gson.Gson

data class MiniAppConfigData(
    val isProduction: Boolean,
    val isPreviewMode: Boolean,
    val isVerificationRequired: Boolean,
    val projectId: String,
    val subscriptionId: String
)


internal fun MiniAppConfigData.toJsonString() = Gson().toJson(this)

internal fun Gson.fromJson(key: String)  = fromJson(key, MiniAppConfigData::class.java)

