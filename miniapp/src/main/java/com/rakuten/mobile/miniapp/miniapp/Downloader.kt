package com.rakuten.mobile.miniapp.miniapp

import com.rakuten.mobile.miniapp.api.ApiClient
import com.rakuten.mobile.miniapp.storage.MiniAppStorage

@SuppressWarnings("UseDataClass")
internal class Downloader(
    val storage: MiniAppStorage,
    val apiClient: ApiClient
)
