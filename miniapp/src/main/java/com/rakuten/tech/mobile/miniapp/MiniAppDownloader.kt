package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

@SuppressWarnings("UseDataClass")
internal class MiniAppDownloader(
    val storage: MiniAppStorage,
    val apiClient: ApiClient
)
