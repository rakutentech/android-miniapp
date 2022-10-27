package com.rakuten.tech.mobile.testapp.ui.settings


data class MiniAppCredentialData(
    val isProduction: Boolean,
    val isVerificationRequired: Boolean,
    val isPreviewMode: Boolean,
    val projectId: String,
    val subscriptionId: String
)
