package com.rakuten.tech.mobile.miniapp.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

@SuppressWarnings("UseDataClass")
internal class Downloader(
    val storage: MiniAppStorage,
    val apiClient: ApiClient
)
