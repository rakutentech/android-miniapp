package com.rakuten.tech.mobile.miniapp.api

@SuppressWarnings("UseDataClass")
internal class ApiClient(
    val listRequest: ListRequest,
    val manifestRequest: ManifestRequest,
    val downloadFileRequest: DownloadFileRequest
)
