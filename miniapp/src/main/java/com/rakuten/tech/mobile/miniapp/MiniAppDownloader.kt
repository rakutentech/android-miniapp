package com.rakuten.tech.mobile.miniapp

import android.content.Context
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

internal class MiniAppDownloader(
    val context: Context,
    val storage: MiniAppStorage,
    val apiClient: ApiClient
) {

    private val prefs = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.downloads",
        Context.MODE_PRIVATE
    )

    suspend fun startDownload(appId: String, versionId: String): String {
        val manifest = fetchManifest(appId, versionId)
        return downloadMiniApp(appId, versionId, manifest)
    }

    suspend fun fetchManifest(
        appId: String,
        versionId: String
    ) = apiClient.fetchFileList(appId, versionId)

    private suspend fun downloadMiniApp(
        appId: String,
        versionId: String,
        manifest: ManifestEntity
    ): String {

        val baseSavePath = storage.getSavePathForApp(appId, versionId)

        if (prefs.getBoolean(versionId, false)) {
            return baseSavePath
        }

        for (file in manifest.files) {
            val response = apiClient.downloadFile(file)
            storage.saveFile(file, baseSavePath, response.byteStream())
        }

        prefs.edit()
            .putBoolean(versionId, true)
            .apply()

        return baseSavePath
    }
}
