package com.rakuten.mobile.miniapp.download.work.scheduler

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.rakuten.mobile.miniapp.core.CoreImpl
import com.rakuten.mobile.miniapp.download.DownloadBaseTest
import com.rakuten.mobile.miniapp.download.work.worker.DownloadWorker
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test file for DownloadScheduler.
 */
@RunWith(AndroidJUnit4::class)
class DownloadSchedulerTest : DownloadBaseTest() {

    private val MANIFEST_ENDPOINT = "https://dev-domain-url.com/miniapp" +
            "/78d85043-d04f-486a-8212-bf2601cb63a2/version/17bccee1-17f0-44fa-8cb8-2da89eb49905/manifest/"
    private lateinit var downloadScheduler: DownloadScheduler
    private lateinit var context: Context

    @Before
    fun setup() {
        context = getApplicationContext()
        CoreImpl.context = context
        downloadScheduler = DownloadScheduler()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    /**
     * If context is not injected, an exception would be thrown.
     */
    @Test
    fun shouldInjectContext() {
        downloadScheduler.context
    }

    @Test
    fun shouldScheduleDownloadViaWorkManager() {
        // Act.
        downloadScheduler.scheduleDownload(MANIFEST_ENDPOINT)
        // Re-arrange data.
        val workInfo = WorkManager.getInstance(context).getWorkInfosByTag(DownloadWorker.WORK_TAG)
        val allTags = mutableSetOf<String>()
        for (info in workInfo.get()) {
            allTags += info.tags
        }
        // Assert.
        assertThat(allTags).contains(DownloadWorker.WORK_TAG)
    }
}
