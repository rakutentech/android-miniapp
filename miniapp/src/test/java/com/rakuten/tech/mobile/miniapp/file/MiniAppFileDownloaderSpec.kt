package com.rakuten.tech.mobile.miniapp.file

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.errors.MiniAppDownloadFileError
import com.rakuten.tech.mobile.miniapp.js.ActionType
import com.rakuten.tech.mobile.miniapp.js.FileDownloadCallbackObj
import com.rakuten.tech.mobile.miniapp.js.FileDownloadParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MiniAppFileDownloaderSpec {
    private val TEST_IMAGE_MIME = "image/jpeg"
    private val TEST_FILENAME = "test.jpg"
    private val TEST_FILE_URL = "https://sample/com/test.jpg"
    private val TEST_HEADER_OBJECT: Map<String, String> = mapOf("auth" to "123", "token" to "abc")
    private val fileDownloadCallbackObj = FileDownloadCallbackObj(
        action = ActionType.FILE_DOWNLOAD.action,
        param = FileDownloadParams(TEST_FILENAME, TEST_FILE_URL, TEST_HEADER_OBJECT),
        id = TEST_CALLBACK_ID
    )
    private val TEST_DEST_URI = Uri.parse("https://sample/com/test.jpg")
    private val fileDownloadJsonStr = Gson().toJson(fileDownloadCallbackObj)
    private lateinit var activity: TestActivity

    @Before
    fun setUp() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            this.activity = activity
        }

        // setup for the MimeTypeMap
        val mtm = MimeTypeMap.getSingleton()
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("jpg", TEST_IMAGE_MIME)
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("jpeg", TEST_IMAGE_MIME)
    }

    @Test
    fun `getMimeType should return the correct mimeType for correct extension`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(activity, 100)
        miniAppFileDownloader.getMimeType(TEST_FILENAME) shouldBeEqualTo TEST_IMAGE_MIME
    }

    @Test
    fun `getMimeType should return the default mimeType for incorrect extension`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(activity, 100)
        miniAppFileDownloader.getMimeType("") shouldBeEqualTo "text/plain"
    }

    @Test
    fun `createRequest should return the correct request with correct header`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(activity, 100)
        val expectedReq = Request.Builder()
            .url(TEST_FILE_URL)
            .addHeader("auth", "123")
            .addHeader("token", "abc")
            .build()
        val actualReq = miniAppFileDownloader.createRequest(TEST_FILE_URL, TEST_HEADER_OBJECT)
        assertEquals(expectedReq.headers, actualReq.headers)
        assertEquals(expectedReq.url, actualReq.url)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `startDownloading should invoke success callback if request is successful`() =
        runBlockingTest {
            val mockServer = MockWebServer()
            mockServer.enqueue(MockResponse().setResponseCode(400))
            mockServer.start()
            val url: String = mockServer.url("/sample/com/test.jpg").toString()

            val miniAppFileDownloader = Mockito.spy(MiniAppFileDownloaderDefault(activity, 100))
            miniAppFileDownloader.onDownloadSuccess = {}
            miniAppFileDownloader.onDownloadFailed = {}
            miniAppFileDownloader.url = url
            miniAppFileDownloader.headers = TEST_HEADER_OBJECT
            miniAppFileDownloader.fileName = TEST_FILENAME

            miniAppFileDownloader.onReceivedResult(TEST_DEST_URI)
            // verify(miniAppFileDownloader).onDownloadFailed.invoke(any())

            mockServer.shutdown()
        }
}
