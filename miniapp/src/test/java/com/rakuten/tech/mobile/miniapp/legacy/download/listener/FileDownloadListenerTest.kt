package com.rakuten.tech.mobile.miniapp.legacy.download.listener

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.legacy.core.CoreImpl
import com.rakuten.tech.mobile.miniapp.legacy.download.DownloadBaseTest
import com.rakuten.tech.mobile.miniapp.legacy.download.utility.MiniAppFileWriter
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks
import retrofit2.Call
import retrofit2.Response

/**
 * Test class for FileDownloadListener.
 */
@RunWith(AndroidJUnit4::class)
class FileDownloadListenerTest : DownloadBaseTest() {

    private val APP_ID = "78d85043-d04f-486a-8212-bf2601cb63a2"
    private val PATH = "/path/example/"
    private lateinit var fileDownloadListener: FileDownloadListener
    @Mock
    lateinit var mockCall: Call<ResponseBody>
    @Mock
    lateinit var mockResponse: Response<ResponseBody>
    @Mock
    lateinit var mockFileWriter: MiniAppFileWriter

    @Before
    fun setup() {
        initMocks(this)
        Mockito.`when`(mockResponse.body()).thenReturn(ResponseBody.create(null, ""))
        CoreImpl.context = getApplicationContext()
        fileDownloadListener =
            FileDownloadListener(
                APP_ID,
                PATH
            )
    }

    @Test
    fun shouldLazyInjectNotThrowException() {
        // Initialize SDK with context.
        CoreImpl.context = getApplicationContext()
        fileDownloadListener.onResponse(mockCall, mockResponse)
    }

    @Test
    fun shouldInvokeWriteResponseBodyToDisk() {
        // Initialize SDK with context.
        fileDownloadListener.fileWriter = mockFileWriter

        fileDownloadListener.onResponse(mockCall, mockResponse)

        verify(fileDownloadListener.fileWriter).writeResponseBodyToDisk(
            mockResponse.body()!!, APP_ID, PATH
        )
    }
}
