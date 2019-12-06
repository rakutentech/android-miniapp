package com.rakuten.mobile.miniapp.download.work.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.rakuten.mobile.miniapp.download.DownloadBaseTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test file for DownloadWorker.
 */
@RunWith(AndroidJUnit4::class)
class DownloadWorkerTest : DownloadBaseTest() {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = getApplicationContext()
    WorkManagerTestInitHelper.initializeTestWorkManager(context)
  }

  @Test
  fun shouldRunDownloadWorkerOnce() {
    val input = workDataOf(DownloadWorker.MANIFEST_URL_ENDPOINT_KEY to MANIFEST_URL)
    val request = OneTimeWorkRequestBuilder<DownloadWorker>().setInputData(input).build()
    val workManager = WorkManager.getInstance(context)
    workManager.enqueue(request)

    val attempts = workManager.getWorkInfoById(request.id).get().runAttemptCount

    assertThat(attempts).isEqualTo(1)
  }

  // TODO: Add more tests which includes network requests and logic in doWork().
}
