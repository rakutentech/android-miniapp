package com.rakuten.mobile.miniapp.miniapp

import com.rakuten.mobile.miniapp.api.ApiClient
import com.rakuten.mobile.miniapp.storage.MiniAppStorage

@SuppressWarnings("UseDataClass")
internal class MiniAppDownloader(
    val miniAppStorage: MiniAppStorage,
    val miniAppStatus: MiniAppStatus,
    val apiClient: ApiClient
)
