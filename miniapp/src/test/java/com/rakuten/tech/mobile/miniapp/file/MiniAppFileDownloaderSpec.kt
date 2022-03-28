package com.rakuten.tech.mobile.miniapp.file

import com.rakuten.tech.mobile.miniapp.js.*

class MiniAppFileDownloaderSpec {
//    private val webViewListener: WebViewListener = mock()
//    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
//    private val activity = TestActivity()
//    private val TEST_FILENAME = "test.jpg"
//    private val TEST_MIME = "test/sample"
//    private val TEST_FILE_PATH = "com/example/test"
//    private val TEST_FILE_URL = "https://sample/com/test.jpg"
//    private val TEST_HEADER_OBJECT = DownloadFileHeaderObj(null)
//    private val fileDownloadCallbackObj = FileDownloadCallbackObj(
//        action = ActionType.FILE_DOWNLOAD.action,
//        param = FileDownloadParams(TEST_FILENAME, TEST_FILE_URL, TEST_HEADER_OBJECT),
//        id = TEST_CALLBACK_ID)
//    private val fileDownloadJsonStr = Gson().toJson(fileDownloadCallbackObj)
//    private val fileDownloader = Mockito.spy(MiniAppFileDownloader::class.java)
//    private val mockMimeTypeMap: MimeTypeMap = mock()
//
//    @Before
//    fun setup() {
//        fileDownloader.activity = activity
//        fileDownloader.bridgeExecutor = bridgeExecutor
//        fileDownloader.mimeTypeMap = mockMimeTypeMap
//
//        When calling mockMimeTypeMap.getMimeTypeFromExtension(".jpg") itReturns TEST_MIME
//        When calling fileDownloader.createFileDirectory(TEST_FILENAME) itReturns File(TEST_FILE_PATH)
//    }
//
//    @Test
//    fun `postError should be called when callbackObject is null`() {
//        val errorMsg = "DOWNLOAD FAILED: Can not parse file download json object"
//        When calling fileDownloader.createFileDownloadCallbackObj(fileDownloadJsonStr) itReturns null
//        fileDownloader.onFileDownload(TEST_CALLBACK_ID, fileDownloadJsonStr)
//        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errorMsg)
//    }
//
//    @Test
//    fun `onStartFileDownload should be called when correct jsonStr`() = runBlockingTest {
//        fileDownloader.onFileDownload(TEST_CALLBACK_ID, fileDownloadJsonStr)
//        verify(fileDownloader).onStartFileDownload(fileDownloadCallbackObj)
//    }
//
//    @Test
//    fun `startDownloading should be called when onStartFileDownload get correct callback`() = runBlockingTest {
//        fileDownloader.onStartFileDownload(fileDownloadCallbackObj)
//        Mockito.doNothing().`when`(fileDownloader).startDownloading(
//            TEST_CALLBACK_ID,
//            TEST_FILENAME,
//            TEST_FILE_URL,
//            TEST_HEADER_OBJECT
//        )
//        verify(fileDownloader).startDownloading(
//            TEST_CALLBACK_ID,
//            TEST_FILENAME,
//            TEST_FILE_URL,
//            TEST_HEADER_OBJECT
//        )
//    }
//
//    @Test
//    fun `postValue should be called when download successful`() = runBlockingTest {
//        val mockServer = MockWebServer()
//        mockServer.enqueue(MockResponse().setBody(""))
//        mockServer.start()
//        val url: String = mockServer.url("/sample/com/test.jpg").toString()
//
//        Mockito.doNothing().`when`(fileDownloader).openShareIntent(TEST_MIME, File(TEST_FILE_PATH))
//
//        fileDownloader.startDownloading(
//            TEST_CALLBACK_ID,
//            TEST_FILENAME,
//            url,
//            TEST_HEADER_OBJECT
//        )
//
//        verify(fileDownloader).writeInputStreamToFile(any(), any())
//        verify(fileDownloader).openShareIntent(any(), any())
//        verify(bridgeExecutor).postValue(TEST_CALLBACK_ID, TEST_FILENAME)
//
//        mockServer.shutdown()
//    }
//
//    @Test
//    fun `postError should be called when download is not successful`() = runBlockingTest {
//        val errorMsg = "DOWNLOAD FAILED: 404"
//        val mockServer = MockWebServer()
//        mockServer.enqueue(MockResponse().setResponseCode(404))
//        mockServer.start()
//        val url: String = mockServer.url("/sample/com/test.jpg").toString()
//
//        fileDownloader.mimeTypeMap = mockMimeTypeMap
//        When calling mockMimeTypeMap.getMimeTypeFromExtension(".jpg") itReturns TEST_MIME
//
//        fileDownloader.startDownloading(
//            TEST_CALLBACK_ID,
//            TEST_FILENAME,
//            url,
//            TEST_HEADER_OBJECT
//        )
//
//        verify(bridgeExecutor).postError(TEST_CALLBACK_ID, errorMsg)
//
//        mockServer.shutdown()
//    }
}
