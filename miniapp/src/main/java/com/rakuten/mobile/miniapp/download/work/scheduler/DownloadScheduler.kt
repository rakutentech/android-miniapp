package com.rakuten.mobile.miniapp.download.work.scheduler

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.rakuten.mobile.miniapp.download.dagger.DaggerDownloadComponent
import com.rakuten.mobile.miniapp.download.work.worker.DownloadWorker
import javax.inject.Inject

/**
 * Scheduling to download MiniApp in the background.
 */
class DownloadScheduler @Inject constructor() {

  /**
   * Dagger injected variable.
   */
  @Inject
  lateinit var context: Context

  init {
    DaggerDownloadComponent.create().inject(this)
  }

  /**
   * Scheduling download MiniApp in the background.
   *
   * @param endpoint Manifest endpoint without base url.
   */
  fun scheduleDownload(endpoint: String) {
    val data = workDataOf(DownloadWorker.MANIFEST_URL_ENDPOINT_KEY to endpoint)

    val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
        .addTag(DownloadWorker.WORK_TAG)
        .setInputData(data)
        .build()

    if (::context.isInitialized) {
      WorkManager.getInstance(context).enqueue(downloadWorkRequest)
    }
  }
}
