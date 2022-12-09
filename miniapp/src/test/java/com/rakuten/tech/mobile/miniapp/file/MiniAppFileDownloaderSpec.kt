package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
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
import kotlinx.coroutines.test.TestCoroutineScope
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.timeout
import org.mockito.kotlin.*
import org.mockito.kotlin.mock
import org.robolectric.Shadows
import java.io.OutputStream
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@Suppress("LargeClass")
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
    private val TEST_BASE64_DATA_URI = "data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAAMAAAADCAAAA" +
            "ABzQ+pjAAAAC0lEQVQI12NgQAAAAAwAAeQ06mYAAAAASUVORK5CYII="
    private val TEST_MIME_TYPE = "image/jpeg"
    private val TEST_EXPECTED_BYTES = byteArrayOf(
        -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82,
        0, 0, 0, 3, 0, 0, 0, 3, 8, 0, 0, 0, 0, 115, 67, -22, 99, 0, 0, 0, 11, 73, 68, 65, 84, 8, -41, 99,
        96, 64, 0, 0, 0, 12, 0, 1, -28, 52, -22, 102, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    )
    private val fileDownloadJsonStr = Gson().toJson(fileDownloadCallbackObj)
    private lateinit var mockServer: MockWebServer
    private val mockContentResolver: ContentResolver = mock()
    private val mockActivity: Activity = mock {
        on { contentResolver } itReturns mockContentResolver
    }
    private lateinit var activity: TestActivity

    @Before
    fun setUp() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            this.activity = activity
        }
        mockServer = MockWebServer()

        // setup for the MimeTypeMap
        val mtm = MimeTypeMap.getSingleton()
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("jpg", TEST_IMAGE_MIME)
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("jpeg", TEST_IMAGE_MIME)
    }

    @After
    fun after() {
        mockServer.shutdown()
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
    fun `startDownloading should invoke success callback if request is successful`() {
            mockServer.enqueue(MockResponse().setBody(""))
            mockServer.start()
            val url: String = mockServer.url("/sample/com/test.jpg").toString()

            val miniAppFileDownloader = Mockito.spy(MiniAppFileDownloaderDefault(activity, 100))
            miniAppFileDownloader.scope = TestCoroutineScope()
            miniAppFileDownloader.onDownloadSuccess = {
                it shouldBe TEST_FILENAME
            }
            miniAppFileDownloader.onDownloadFailed = {}
            miniAppFileDownloader.url = url
            miniAppFileDownloader.headers = TEST_HEADER_OBJECT
            miniAppFileDownloader.fileName = TEST_FILENAME

            miniAppFileDownloader.onReceivedResult(TEST_DEST_URI)
            verify(miniAppFileDownloader).onDownloadSuccess
        }

    @Test
    fun `startDownloading should invoke fail callback if request isn't successful`() {
        mockServer.enqueue(MockResponse().setResponseCode(400))
        mockServer.start()
        val url: String = mockServer.url("/sample/com/test.jpg").toString()

        val miniAppFileDownloader = Mockito.spy(MiniAppFileDownloaderDefault(activity, 100))
        miniAppFileDownloader.scope = TestCoroutineScope()
        miniAppFileDownloader.onDownloadSuccess = {
            it shouldBe TEST_FILENAME
        }
        miniAppFileDownloader.onDownloadFailed = {}
        miniAppFileDownloader.url = url
        miniAppFileDownloader.headers = TEST_HEADER_OBJECT
        miniAppFileDownloader.fileName = TEST_FILENAME

        miniAppFileDownloader.onReceivedResult(TEST_DEST_URI)
        verify(miniAppFileDownloader).onDownloadFailed
    }

    @Test
    fun `onStartFileDownload when called with data URI should launch an Intent to create a new document`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)

        miniAppFileDownloader.onStartFileDownload(TEST_FILENAME, TEST_BASE64_DATA_URI, TEST_HEADER_OBJECT, {}, {})

        verify(mockActivity).startActivityForResult(
            argWhere { intent ->
                intent.action == Intent.ACTION_CREATE_DOCUMENT &&
                        intent.type == TEST_MIME_TYPE &&
                        intent.extras!!.getString(Intent.EXTRA_TITLE) == TEST_FILENAME
            },
            eq(100)
        )
    }

    @Test
    fun `onStartFileDownload when called with an HTTPS URL should launch an Intent to create a new document`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)

        miniAppFileDownloader.onStartFileDownload(TEST_FILENAME, TEST_FILE_URL, TEST_HEADER_OBJECT, {}, {})

        verify(mockActivity).startActivityForResult(
            argWhere { intent ->
                intent.action == Intent.ACTION_CREATE_DOCUMENT &&
                        intent.type == TEST_MIME_TYPE &&
                        intent.extras!!.getString(Intent.EXTRA_TITLE) == TEST_FILENAME
            },
            eq(100)
        )
    }

    @Test
    fun `onStartFileDownload when called with invalid URL should return InvalidUrlError to onFailedDownload`() {
        val onFailed: (MiniAppDownloadFileError) -> Unit = mock()
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)

        miniAppFileDownloader.onStartFileDownload(
            TEST_FILENAME,
            "test-invalid-url://test",
            TEST_HEADER_OBJECT,
            {},
            onFailed
        )

        verify(onFailed).invoke(MiniAppDownloadFileError.invalidUrlError)
    }

    @Test
    fun `onReceivedResult when called with data URI should call onSuccess`() {
        val destinationUri: Uri = mock()
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(activity, 100)
        val onSuccess: (String) -> Unit = mock()

        miniAppFileDownloader.onStartFileDownload(
            TEST_FILENAME,
            TEST_BASE64_DATA_URI,
            TEST_HEADER_OBJECT,
            onSuccess,
            {}
        )
        miniAppFileDownloader.onReceivedResult(destinationUri)

        verify(onSuccess, timeout(100)).invoke(TEST_FILENAME)
    }

    @Test
    fun `onReceivedResult when called with data URI should write the file data to the URI location chosen by user`() {
        val destinationUri: Uri = mock()
        val outputStream: OutputStream = mock()
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)
        When calling mockContentResolver.openOutputStream(destinationUri) itReturns outputStream

        miniAppFileDownloader.onStartFileDownload(TEST_FILENAME, TEST_BASE64_DATA_URI, TEST_HEADER_OBJECT, {}, {})
        miniAppFileDownloader.onReceivedResult(destinationUri)

        verify(outputStream).write(argWhere {
            val byteArray = it.copyOfRange(0, TEST_EXPECTED_BYTES.size) // trim buffer bytes from end
            byteArray.contentEquals(TEST_EXPECTED_BYTES)
        }, any(), any())
    }

    @Test
    fun `onReceivedResult when fails to decode data URI should return error to onFailedDownload`() {
        val invalidDataUri = "data:image/png;base64,12345"
        val onFailed: (MiniAppDownloadFileError) -> Unit = mock()
        val destinationUri: Uri = mock()
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)

        miniAppFileDownloader.onStartFileDownload(TEST_FILENAME, invalidDataUri, TEST_HEADER_OBJECT, {}, onFailed)
        miniAppFileDownloader.onReceivedResult(destinationUri)

        verify(onFailed).invoke(MiniAppDownloadFileError.saveFailureError)
    }

    @Test
    fun `error descriptions of MiniAppDownloadFileError should be found as expected`() {
        assertEquals(
            MiniAppDownloadFileError.errorDescription(MiniAppDownloadFileError.DownloadFailedError),
            "Failed to download the file."
        )
        assertEquals(
            MiniAppDownloadFileError.errorDescription(MiniAppDownloadFileError.InvalidUrlError),
            "URL is invalid."
        )
        assertEquals(
            MiniAppDownloadFileError.errorDescription(MiniAppDownloadFileError.SaveFailureError),
            "Save file temporarily failed"
        )
        assertEquals(MiniAppDownloadFileError.errorDescription(""), "")
    }
}
