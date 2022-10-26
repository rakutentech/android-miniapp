package com.rakuten.tech.mobile.testapp.ui.settings

data class MiniAppCredentialData(
    val isProduction: Boolean,
    val isPreviewMode: Boolean,
    val requireSignatureVerification: Boolean,
    val projectId: String,
    val subscriptionId: String
)
