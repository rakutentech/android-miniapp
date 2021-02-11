package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import com.rakuten.tech.mobile.miniapp.storage.MiniAppLocal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

@Suppress("SwallowedException", "TooManyFunctions")
internal class MiniAppDownloader(
    context: Context,
    private var apiClient: ApiClient,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UpdatableApiClient {
    private val miniAppLocal = MiniAppLocal(context)
    
    suspend fun getMiniApp(appId: String): Pair<String, MiniAppInfo> = try {
        val miniAppInfo = apiClient.fetchInfo(appId)
        onGetMiniApp(miniAppInfo)
    } catch (netError: MiniAppNetException) {
        onNetworkError(miniAppLocal.getMiniAppStatus().getDownloadedMiniApp(appId))
    }

    suspend fun getMiniApp(miniAppInfo: MiniAppInfo): Pair<String, MiniAppInfo> = try {
        onGetMiniApp(miniAppInfo)
    } catch (netError: MiniAppNetException) {
        onNetworkError(miniAppInfo)
    }

    @Suppress("ThrowsCount")
    fun validateHttpAppUrl(url: String) {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            val validHTTPResponseCodes = 100..399
            val code = connection.responseCode
            if (code !in validHTTPResponseCodes) {
                throw MiniAppSdkException("Invalid URL error")
            }
        } catch (netError: ConnectException) {
            throw MiniAppNotFoundException("Invalid URL error")
        } catch (ioError: IOException) {
            throw sdkExceptionForInternalServerError()
        } finally {
            connection?.disconnect()
        }
    }

    private suspend fun onGetMiniApp(miniAppInfo: MiniAppInfo): Pair<String, MiniAppInfo> {
        val downloadedVersionPath = retrieveDownloadedVersionPath(miniAppInfo)

        return if (downloadedVersionPath == null) {
            val versionPath = startDownload(miniAppInfo)
            miniAppLocal.getVerifier().storeHashAsync(miniAppInfo.version.versionId, File(versionPath))
            storeDownloadedMiniApp(miniAppInfo)

            Pair(versionPath, miniAppInfo)
        } else {
            Pair(downloadedVersionPath, miniAppInfo)
        }
    }

    private fun onNetworkError(miniAppInfo: MiniAppInfo?): Pair<String, MiniAppInfo> {
        if (miniAppInfo != null) {
            val downloadedVersionPath = retrieveDownloadedVersionPath(miniAppInfo)
            if (downloadedVersionPath !== null) {
                return Pair(downloadedVersionPath, miniAppInfo)
            }
        }
        // cannot load miniapp from server
        throw sdkExceptionForInternalServerError()
    }

    private fun storeDownloadedMiniApp(miniAppInfo: MiniAppInfo) {
        miniAppLocal.getMiniAppStatus().setVersionDownloaded(miniAppInfo.id, miniAppInfo.version.versionId, true)
        miniAppLocal.getMiniAppStatus().saveDownloadedMiniApp(miniAppInfo)
    }

    private fun retrieveDownloadedVersionPath(miniAppInfo: MiniAppInfo): String? {
        val versionPath = miniAppLocal.getMiniAppStorage().getMiniAppVersionPath(miniAppInfo.id, miniAppInfo.version.versionId)

        if (miniAppLocal.getMiniAppStatus().isVersionDownloaded(miniAppInfo.id, miniAppInfo.version.versionId, versionPath)) {
            return if (miniAppLocal.getVerifier().verify(miniAppInfo.version.versionId, File(versionPath)))
                versionPath
            else {
                Log.e(
                    TAG, "Failed to verify the hash of the cached files. " +
                            "The files will be deleted and the Mini App re-downloaded."
                )
                miniAppLocal.getMiniAppStorage().removeApp(miniAppInfo.id)
                miniAppLocal.getMiniAppStatus().setVersionDownloaded(miniAppInfo.id, miniAppInfo.version.versionId, false)
                null
            }
        }
        return null
    }

    @VisibleForTesting
    suspend fun startDownload(miniAppInfo: MiniAppInfo): String {
        val manifest = fetchManifest(miniAppInfo.id, miniAppInfo.version.versionId)
        return downloadMiniApp(miniAppInfo, manifest)
    }

    @VisibleForTesting
    suspend fun fetchManifest(
        appId: String,
        versionId: String
    ) = apiClient.fetchFileList(appId, versionId)

    @SuppressWarnings("LongMethod")
    private suspend fun downloadMiniApp(
        miniAppInfo: MiniAppInfo,
        manifest: ManifestEntity
    ): String {
        val appId = miniAppInfo.id
        val versionId = miniAppInfo.version.versionId
        val baseSavePath = miniAppLocal.getMiniAppStorage().getMiniAppVersionPath(appId, versionId)
        when {
            isManifestValid(manifest) -> {
                for (file in manifest.files) {
                    val response = apiClient.downloadFile(file)
                    miniAppLocal.getMiniAppStorage().saveFile(file, baseSavePath, response.byteStream())
                }
                if (!apiClient.isPreviewMode) {
                    withContext(coroutineDispatcher) {
                        launch(Job()) { miniAppLocal.getMiniAppStorage().removeVersions(appId, versionId) }
                    }
                }
                return baseSavePath
            }
            // If backend functions correctly, this should never happen
            else -> throw sdkExceptionForInternalServerError()
        }
    }

    fun getDownloadedMiniAppList(): List<MiniAppInfo> =
        miniAppLocal.getMiniAppStatus().getDownloadedMiniAppList()

    @Suppress("SENSELESS_COMPARISON")
    @VisibleForTesting
    internal fun isManifestValid(manifest: ManifestEntity) =
        manifest != null && manifest.files != null && manifest.files.isNotEmpty()

    override fun updateApiClient(apiClient: ApiClient) {
        this.apiClient = apiClient
    }

    companion object {
        private const val TAG = "MiniAppDownloader"
    }
}
