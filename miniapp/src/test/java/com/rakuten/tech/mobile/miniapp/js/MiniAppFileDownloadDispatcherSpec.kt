package com.rakuten.tech.mobile.miniapp.js

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileDownloaderDefault
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class MiniAppFileDownloadDispatcherSpec {
    private val testFileName = "test.jpg"
    private val testFileUrl = "https://sample/com/test.jpg"
    private val testHeaders: Map<String, String> = mock()
    private val fileDownloadCallbackObj = FileDownloadCallbackObj(
        action = ActionType.FILE_DOWNLOAD.action,
        param = FileDownloadParams(testFileName, testFileUrl, testHeaders),
        id = TEST_CALLBACK_ID
    )
    private val fileDownloadJsonStr = Gson().toJson(fileDownloadCallbackObj)
    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
    private lateinit var miniAppFileDownloadDispatcher: MiniAppFileDownloadDispatcher
    private val activity: TestActivity = mock()
    private val customPermissionCache: MiniAppCustomPermissionCache = mock()

    @Before
    fun setUp() {
        miniAppFileDownloadDispatcher = MiniAppFileDownloadDispatcher()
        miniAppFileDownloadDispatcher.setBridgeExecutor(mock(), bridgeExecutor)
        miniAppFileDownloadDispatcher.setMiniAppComponents(TEST_MA_ID, customPermissionCache)
        whenever(customPermissionCache.hasPermission(
            TEST_MA_ID, MiniAppCustomPermissionType.FILE_DOWNLOAD)
        ).thenReturn(true)
        // setup for the MimeTypeMap
        val mtm = MimeTypeMap.getSingleton()
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("jpg", "image/jpeg")
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("jpeg", "image/jpeg")
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("png", "image/png")
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("gif", "image/gif")
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("pdf", "application/pdf")
    }

    @Test
    fun `createFileDownloadCallbackObj should return the correct callbackObject`() {
        miniAppFileDownloadDispatcher.createFileDownloadCallbackObj(
            fileDownloadJsonStr
        ) shouldBeEqualTo fileDownloadCallbackObj
    }

    @Test
    fun `createFileDownloadCallbackObj should return null if incorrect jsonStr`() {
        miniAppFileDownloadDispatcher.createFileDownloadCallbackObj("") shouldBeEqualTo null
    }

    @Test
    fun `onFileDownload should not be working if initialization is not completed`() {
        val miniAppFileDownloadDispatcher = Mockito.spy(MiniAppFileDownloadDispatcher())
        miniAppFileDownloadDispatcher.onFileDownload(TEST_CALLBACK_ID, fileDownloadJsonStr)
        verify(miniAppFileDownloadDispatcher, times(0)).createFileDownloadCallbackObj(
            fileDownloadJsonStr
        )
    }

    @Test
    fun `postError should be called if callback object is null`() {
        val errorMsg = "DOWNLOAD FAILED: Can not parse file download json object"
        miniAppFileDownloadDispatcher.setFileDownloader(mock())
        miniAppFileDownloadDispatcher.onFileDownload(TEST_CALLBACK_ID, "")
        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errorMsg)
    }

    @Test
    fun `postError should be called if file download permission is unavailable`() {
        val errorMsg = "DOWNLOAD FAILED: Permission has not been accepted yet for downloading files."
        miniAppFileDownloadDispatcher.setFileDownloader(mock())
        whenever(customPermissionCache.hasPermission(
            TEST_MA_ID, MiniAppCustomPermissionType.FILE_DOWNLOAD)
        ).thenReturn(false)
        miniAppFileDownloadDispatcher.onFileDownload(TEST_CALLBACK_ID, "")
        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errorMsg)
    }

    @Test
    fun `onStartFileDownload should be called if callback object is not null`() {
        val miniAppFileDownloader = Mockito.spy(MiniAppFileDownloaderDefault(activity, 100))
        miniAppFileDownloadDispatcher.setFileDownloader(miniAppFileDownloader = miniAppFileDownloader)
        miniAppFileDownloadDispatcher.onFileDownload(TEST_CALLBACK_ID, fileDownloadJsonStr)
        verify(miniAppFileDownloader).openCreateDocIntent(activity, testFileName)
    }
}
