package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.errors.MiniAppDownloadFileError
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
import org.robolectric.util.ReflectionHelpers
import java.io.OutputStream
import java.lang.NullPointerException
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@Suppress("LargeClass")
class MiniAppFileDownloaderSpec {
    private val testImageMime = "image/jpeg"
    private val testFileName = "test.jpg"
    private val testFileUrl = "https://sample/com/test.jpg"
    private val testHeaders: Map<String, String> = mapOf("auth" to "123", "token" to "abc")
    private val testDestUri = Uri.parse("https://sample/com/test.jpg")
    private val testBase64DataUri = "data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAAMAAAADCAAAA" +
            "ABzQ+pjAAAAC0lEQVQI12NgQAAAAAwAAeQ06mYAAAAASUVORK5CYII="
    private val testMimeType = "image/jpeg"
    private val testExpectedBytes = byteArrayOf(
        -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82,
        0, 0, 0, 3, 0, 0, 0, 3, 8, 0, 0, 0, 0, 115, 67, -22, 99, 0, 0, 0, 11, 73, 68, 65, 84, 8, -41, 99,
        96, 64, 0, 0, 0, 12, 0, 1, -28, 52, -22, 102, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    )
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
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("jpg", testImageMime)
        Shadows.shadowOf(mtm).addExtensionMimeTypMapping("jpeg", testImageMime)
    }

    @After
    fun after() {
        mockServer.shutdown()
    }

    @Test
    fun `getMimeType should return the correct mimeType for correct extension`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(activity, 100)
        miniAppFileDownloader.getMimeType(testFileName) shouldBeEqualTo testImageMime
    }

    @Test
    fun `getMimeType should return the default mimeType for incorrect extension`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(activity, 100)
        miniAppFileDownloader.getMimeType("") shouldBeEqualTo "text/plain"
    }

    @Test
    fun `getMimeType should return the all mimeType for android api level 29`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(activity, 100)
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 29)
        miniAppFileDownloader.getMimeType(TEST_FILENAME) shouldBeEqualTo "*/*"
    }

    @Test
    fun `createRequest should return the correct request with correct header`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(activity, 100)
        val expectedReq = Request.Builder()
            .url(testFileUrl)
            .addHeader("auth", "123")
            .addHeader("token", "abc")
            .build()
        val actualReq = miniAppFileDownloader.createRequest(testFileUrl, testHeaders)
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
                it shouldBe testFileName
            }
            miniAppFileDownloader.onDownloadFailed = {}
            miniAppFileDownloader.url = url
            miniAppFileDownloader.headers = testHeaders
            miniAppFileDownloader.fileName = testFileName

            miniAppFileDownloader.onReceivedResult(testDestUri)
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
            it shouldBe testFileName
        }
        miniAppFileDownloader.onDownloadFailed = {}
        miniAppFileDownloader.url = url
        miniAppFileDownloader.headers = testHeaders
        miniAppFileDownloader.fileName = testFileName

        miniAppFileDownloader.onReceivedResult(testDestUri)
        verify(miniAppFileDownloader).onDownloadFailed
    }

    @Test
    fun `onStartFileDownload when called with data URI should launch an Intent to create a new document`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)

        miniAppFileDownloader.onStartFileDownload(testFileName, testBase64DataUri, testHeaders, {}, {})

        verify(mockActivity).startActivityForResult(
            argWhere { intent ->
                intent.action == Intent.ACTION_CREATE_DOCUMENT &&
                        intent.type == testMimeType &&
                        intent.extras!!.getString(Intent.EXTRA_TITLE) == testFileName
            },
            eq(100)
        )
    }

    @Test
    fun `onStartFileDownload when called with an HTTPS URL should launch an Intent to create a new document`() {
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)

        miniAppFileDownloader.onStartFileDownload(testFileName, testFileUrl, testHeaders, {}, {})

        verify(mockActivity).startActivityForResult(
            argWhere { intent ->
                intent.action == Intent.ACTION_CREATE_DOCUMENT &&
                        intent.type == testMimeType &&
                        intent.extras!!.getString(Intent.EXTRA_TITLE) == testFileName
            },
            eq(100)
        )
    }

    @Test
    fun `onStartFileDownload when called with invalid URL should return InvalidUrlError to onFailedDownload`() {
        val onFailed: (MiniAppDownloadFileError) -> Unit = mock()
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)

        miniAppFileDownloader.onStartFileDownload(
            testFileName,
            "test-invalid-url://test",
            testHeaders,
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
            testFileName,
            testBase64DataUri,
            testHeaders,
            onSuccess
        ) {}
        miniAppFileDownloader.onReceivedResult(destinationUri)

        verify(onSuccess, timeout(100)).invoke(testFileName)
    }

    @Test
    fun `onReceivedResult when called with data URI should write the file data to the URI location chosen by user`() {
        val destinationUri: Uri = mock()
        val outputStream: OutputStream = mock()
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)
        When calling mockContentResolver.openOutputStream(destinationUri) itReturns outputStream

        miniAppFileDownloader.onStartFileDownload(testFileName, testBase64DataUri, testHeaders, {}, {})
        miniAppFileDownloader.onReceivedResult(destinationUri)

        verify(outputStream).write(argWhere {
            val byteArray = it.copyOfRange(0, testExpectedBytes.size) // trim buffer bytes from end
            byteArray.contentEquals(testExpectedBytes)
        }, any(), any())
    }

    @Test
    fun `onReceivedResult when fails to decode data URI should return error to onFailedDownload`() {
        val invalidDataUri = "data:image/png;base64,12345"
        val onFailed: (MiniAppDownloadFileError) -> Unit = mock()
        val destinationUri: Uri = mock()
        val miniAppFileDownloader = MiniAppFileDownloaderDefault(mockActivity, 100)

        miniAppFileDownloader.onStartFileDownload(testFileName, invalidDataUri, testHeaders, {}, onFailed)
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

    @Test(expected = NullPointerException::class)
    fun `onCancel should invoke onDownloadSuccess`() {
        val miniAppFileDownloader = spy(MiniAppFileDownloaderDefault(activity, 100))
        miniAppFileDownloader.onDownloadSuccess = {
            it shouldBe "null"
        }
        miniAppFileDownloader.onCancel()
        verify(miniAppFileDownloader).onDownloadSuccess.invoke("null")
    }
}
