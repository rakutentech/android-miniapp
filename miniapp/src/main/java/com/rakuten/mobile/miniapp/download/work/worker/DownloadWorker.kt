package com.rakuten.mobile.miniapp.download.work.worker

import android.content.Context
import androidx.work.ListenableWorker.Result.failure
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rakuten.mobile.miniapp.core.utils.LocalUrlParser
import com.rakuten.mobile.miniapp.download.DownloadMiniAppImpl
import com.rakuten.mobile.miniapp.download.listener.FileDownloadListener
import com.rakuten.mobile.miniapp.download.network.api.MiniAppServiceApi
import com.rakuten.mobile.miniapp.download.network.client.RetrofitClient
import timber.log.Timber
import javax.inject.Inject

/**
 * DownloadWorker will first talk to /manifest in order to retrieve a list of URLs which points to
 * all files of the MiniApp.
 */
class DownloadWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    /**
     * Dagger injected variable.
     */
    @Inject
    lateinit var retrofitClient: RetrofitClient

    init {
        DownloadMiniAppImpl.daggerDownloadComponent.inject(this)
    }

    /**
     * Communicating with /manifest endpoint, then download files on the manifest.
     */
    override fun doWork(): Result {
        val manifestUrl = inputData.getString(MANIFEST_URL_ENDPOINT_KEY) ?: return failure()

        Timber.tag(TAG).d(manifestUrl)

        // Retrieve manifest.
        val manifestRequest =
            retrofitClient.retrofit.create(MiniAppServiceApi::class.java).getManifest(manifestUrl)
        // Execute synchronized network call to /manifest endpoint.
        val manifestResponse = manifestRequest.execute()
        val manifest = manifestResponse.body()?.manifest

        if (manifest.isNullOrEmpty()) {
            return success()
        }

        Timber.tag(TAG).d("%s files in the manifest.", manifest.size.toString())

        // Download all files on the manifest.
        for (url in manifest as ArrayList) {
            // TODO: Remove this after BE is fixed.
            val fixedUrl = url.replace("https:/", "https://")
            Timber.tag(TAG).d(fixedUrl)

            val fileDownloadRequest =
                retrofitClient.retrofit.create(MiniAppServiceApi::class.java).getFile(fixedUrl)
            fileDownloadRequest.enqueue(
                FileDownloadListener(
                    LocalUrlParser().getAppId(manifestUrl),
                    fixedUrl
                )
            )
            Timber.tag(TAG).d("Downloading file url: $fixedUrl")
        }

        return success()
    }

    companion object {
        const val WORK_TAG = "mini_app_download_task"
        const val MANIFEST_URL_ENDPOINT_KEY = "mini_app_manifest_url_endpoint"
        private const val TAG = "Mini_DownloadWorker"
    }
}
