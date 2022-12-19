package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.TestCoroutineScope
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import java.io.File

@RunWith(AndroidJUnit4::class)
class WebViewFileDownloaderSpec {

    @Rule @JvmField
    val temporaryFolder = TemporaryFolder()
    private val context: Context = mock()
    private val scope = TestCoroutineScope()
    private val fileProvider: DefaultFileProvider = mock()

    private val base64Data = "iVBORw0KGgoAAAANSUhEUgAAAAMAAAADCAAAAABzQ+pjAAAAC0lEQVQI12NgQAAAAAwAA" +
            "eQ06mYAAAAASUVORK5CYII="
    private val dataUrl = "data:image/png;base64,$base64Data"
    private val mimeType = "image/png"

    private lateinit var fileDownloader: WebViewFileDownloader
    private lateinit var cache: File

    @Before
    fun setup() {
        cache = temporaryFolder.newFolder()
        fileDownloader = WebViewFileDownloader(context, cache, scope, mock(), fileProvider)
    }

    @Test
    fun `onDownloadStart should launch a Chooser Intent`() {
        fileDownloader.onDownloadStart(dataUrl, mimeType) {}

        Verify on context that context.startActivity(argThat {
            action == Intent.ACTION_CHOOSER
        }) was called
    }

    @Test
    fun `onDownloadStart should not launch a Chooser Intent when wrong url`() {
        fileDownloader.onDownloadStart("abc:url", mimeType) {}

        VerifyNotCalled on context that context.startActivity(argThat {
            action == Intent.ACTION_CHOOSER
        }) was called
    }

    @Test
    fun `cleanup should delete the cache folder`() {
        fileDownloader.onDownloadStart(dataUrl, mimeType) {}
        fileDownloader.cleanup()

        cache.exists() shouldBe false
    }

    @Test
    fun `defaultFileProvider should be FileProvider`() {
        val defaultFileProvider = MiniAppDefaultFileProvider()
        defaultFileProvider shouldBeInstanceOf FileProvider::class
    }
}
