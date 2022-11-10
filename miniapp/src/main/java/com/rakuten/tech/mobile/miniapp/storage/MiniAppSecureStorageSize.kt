package com.rakuten.tech.mobile.miniapp.storage

import androidx.annotation.Keep

/**
 * A data class to return used and max DB size.
 */
@Keep
internal data class MiniAppSecureStorageSize(
    val used: Long,
    val max: Long
)
