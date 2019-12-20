package com.rakuten.mobile.miniapp.legacy.download.utility

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.rakuten.mobile.miniapp.legacy.core.CoreImpl
import com.rakuten.mobile.miniapp.legacy.download.DownloadBaseTest
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks

/**
 * Test class for MiniAppFileWriter.
 */
@RunWith(AndroidJUnit4::class)
class MiniAppFileWriterTest : DownloadBaseTest() {

    @Mock
    private lateinit var responseBody: ResponseBody
    private lateinit var miniWriter: MiniAppFileWriter

    @Before
    fun setup() {
        CoreImpl.context = getApplicationContext()
        initMocks(this)
        miniWriter = MiniAppFileWriter()
    }

    @Test
    fun shouldInjectUrlParser() {
        assertThat(miniWriter.localUrlParser).isNotNull()
    }

    @Test
    fun shouldWriteNotThrowException() {
        miniWriter.writeResponseBodyToDisk(responseBody, "appid", "fileURL")
    }
}
