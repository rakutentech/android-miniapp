package com.rakuten.mobile.miniapp

/**
 * This represents the contract between the consuming application and the SDK
 * by which operations in the mini app ecosystem are exposed.
 */
interface MiniApp {

    /**
     * Provides a [List] of type [MiniAppInfo] when obtained successfully, and an
     * [error] when fetching fails from the backend server for any reason.
     */
    suspend fun listMiniApp(
        success: (List<MiniAppInfo>) -> Unit,
        error: (Exception) -> Unit
    )

    /**
     * Creates a mini app for the given [appId]. The mini app is downloaded and saved.
     * Provides [MiniAppView] when successful and an [error] when there is some issue
     * during fetching, downloading or creating a view.
     */
    suspend fun create(
        appId: String,
        success: (MiniAppView) -> Unit,
        error: (Exception) -> Unit
    )

    /**
     * Creates a mini app for the given [qrCode] which is then parsed with the [QrCodeParser]
     * The mini app is downloaded and saved.
     * Provides [MiniAppView] when successful and an [error] when there is some issue
     * during parsing, fetching, downloading or creating a view.
     */
    suspend fun createFromQrCode(
        qrCode: String,
        success: (MiniAppView) -> Unit,
        error: (Exception) -> Unit
    )
}
