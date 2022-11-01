package com.rakuten.tech.mobile.miniapp.storage

/**
 * A data class to return used and max DB size.
 */
internal data class MiniAppSecureStorageSize(
    val used: Long,
    val max: Long
)
