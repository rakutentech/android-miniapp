package com.rakuten.tech.mobile.miniapp.storage

import androidx.annotation.Keep
import com.rakuten.tech.mobile.miniapp.MiniAppManifest

@Keep
internal data class CachedManifest(
    val versionId: String,
    val miniAppManifest: MiniAppManifest
)
