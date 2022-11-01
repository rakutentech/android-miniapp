package com.rakuten.tech.mobile.testapp.ui.settings

data class MiniAppConfigData(
    val isProduction: Boolean,
    val isPreviewMode: Boolean,
    val isVerificationRequired: Boolean,
    val projectId: String,
    val subscriptionId: String
)
