package com.rakuten.tech.mobile.miniapp

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Test

class MiniAppDownloaderSpec {

    @Test
    fun `when downloading a mini app then downloader should fetch manifest at first`() =
        runBlockingTest {
            val apiClient: ApiClient = mock()
            val storage: MiniAppStorage = mock()
            val downloader = MiniAppDownloader(storage, apiClient)
            When calling downloader.fetchManifest(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns ManifestEntity(listOf())

            downloader.startDownload(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
            verify(apiClient, times(1)).fetchFileList(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }

    @Test(expected = MiniAppSdkException::class)
    fun `when fetching manifest fails then downloader should throw exception`() =
        runBlockingTest {
            val downloader = MiniAppDownloader(mock(), mock())
            downloader.startDownload(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }
}
