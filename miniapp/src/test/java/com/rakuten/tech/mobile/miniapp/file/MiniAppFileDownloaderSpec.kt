package com.rakuten.tech.mobile.miniapp.file

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.*
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MiniAppFileDownloaderSpec {
    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
    private val activity = TestActivity()
    private val fileDownloadCallbackObj = FileDownloadCallbackObj(
        action = ActionType.FILE_DOWNLOAD.action,
        param = FileDownloadParams("test", "https://", DownloadFileHeaderObj(null)),
        id = TEST_CALLBACK_ID)
    private val fileDownloadJsonStr = Gson().toJson(fileDownloadCallbackObj)
    private val fileDownloader = Mockito.spy(MiniAppFileDownloader::class.java)

    @Before
    fun setup() {
        fileDownloader.activity = activity
        fileDownloader.bridgeExecutor = bridgeExecutor

        When calling fileDownloader.createFileDirectory("test") itReturns mock()
    }

    @Test
    fun `postError should be called when callbackObject is null`() {
        val errorMsg = "DOWNLOAD FAILED: Can not parse file download json object"
        When calling fileDownloader.createFileDownloadCallbackObj(fileDownloadJsonStr) itReturns null
        fileDownloader.onFileDownload(TEST_CALLBACK_ID, fileDownloadJsonStr)
        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errorMsg)
    }

    @Test
    fun `onStartFileDownload should be called when correct jsonStr`() = runBlockingTest {
        fileDownloader.onFileDownload(TEST_CALLBACK_ID, fileDownloadJsonStr)
        verify(fileDownloader).onStartFileDownload(fileDownloadCallbackObj)
    }

    @Test
    fun `startDownloading should be called when onStartFileDownload get correct callback`() = runBlockingTest {
        fileDownloader.onStartFileDownload(fileDownloadCallbackObj)
        verify(fileDownloader).startDownloading(
            TEST_CALLBACK_ID,
            "test",
            "https://",
            DownloadFileHeaderObj(null)
        )
    }
}
