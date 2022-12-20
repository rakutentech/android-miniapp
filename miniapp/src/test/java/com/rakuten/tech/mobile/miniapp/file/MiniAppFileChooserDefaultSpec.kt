package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TestActivity
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SuppressWarnings("LargeClass")
@RunWith(AndroidJUnit4::class)
class MiniAppFileChooserDefaultSpec {
    private lateinit var miniAppFileChooser: MiniAppFileChooserDefault
    private lateinit var context: Context
    private val requestCode = 100
    private val callback: ValueCallback<Array<Uri>> = mock()
    private var fileChooserParams: WebChromeClient.FileChooserParams? = mock()
    private val intent: Intent = mock()

    private val jpegMimeType = "image/jpeg"
    private val pngMimeType = "image/png"

    private val gifMimeType = "image/gif"

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            context = spy(activity)
            whenever(fileChooserParams?.createIntent()).thenReturn(intent)
            miniAppFileChooser = spy(MiniAppFileChooserDefault(requestCode))
        }
        // setup for the MimeTypeMap
        val mtm = MimeTypeMap.getSingleton()
        shadowOf(mtm).addExtensionMimeTypMapping("jpg", jpegMimeType)
        shadowOf(mtm).addExtensionMimeTypMapping("jpeg", jpegMimeType)
        shadowOf(mtm).addExtensionMimeTypMapping("png", pngMimeType)
        shadowOf(mtm).addExtensionMimeTypMapping("gif", gifMimeType)
        shadowOf(mtm).addExtensionMimeTypMapping("pdf", "application/pdf")
    }

    @Test
    fun `onShowFileChooser should assign filePathCallback correctly`() {
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        assertEquals(miniAppFileChooser.callback, callback)
    }

    @Test
    fun `onShowFileChooser should create intent correctly`() {
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        verify(fileChooserParams)?.createIntent()
    }

    @Test
    fun `onShowFileChooser should not create intent when null`() {
        miniAppFileChooser.onShowFileChooser(callback, null, context)
        verify(fileChooserParams, times(0))?.createIntent()
    }

    @Test
    fun `onShowFileChooser should startActivityForResult correctly`() {
        val actual = miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        verify(context as Activity).startActivityForResult(intent, requestCode)
        assertTrue(actual)
    }

    @Test
    fun `onShowFileChooser should return false when there is exception`() {
        val mockContext: Context = mock()
        val actual = miniAppFileChooser.onShowFileChooser(mock(), mock(), mockContext)
        assertFalse(actual)
    }

    @Test
    fun `onShowFileChooser should launch camera permission when isCaptureEnabled`() {
        whenever(fileChooserParams?.isCaptureEnabled).thenReturn(true)
        val actual = miniAppFileChooser.onShowFileChooser(mock(), fileChooserParams, context)
        verify(miniAppFileChooser).launchCameraIntent()
        assertTrue(actual)
    }

    @Test
    fun `onShowFileChooser should add EXTRA_MIME_TYPES when acceptTypes is not empty`() {
        val fileChooserParamsAcceptTypes = arrayOf(pngMimeType, "image/jpg")
        whenever(fileChooserParams?.acceptTypes).itReturns(fileChooserParamsAcceptTypes)
        miniAppFileChooser.onShowFileChooser(
            mock(),
            fileChooserParams,
            context,
        )
        verify(context as Activity).startActivityForResult(intent, requestCode)
    }

    @Test
    fun `onShowFileChooser should return true when acceptTypes is not empty`() {
        val fileChooserParamsAcceptTypes = arrayOf(pngMimeType, "image/jpg")
        whenever(fileChooserParams?.acceptTypes).itReturns(fileChooserParamsAcceptTypes)
        val actual = miniAppFileChooser.onShowFileChooser(
            mock(),
            fileChooserParams,
            context,
        )
        assertTrue(actual)
    }

    @Test
    fun `onShowFileChooser Intent should add EXTRA_ALLOW_MULTIPLE extra if isOpenMultipleMode is true`() {
        whenever(fileChooserParams?.mode).thenReturn(WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE)
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        verify(fileChooserParams?.createIntent())?.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }

    @Test
    fun `onShowFileChooser should return true when isOpenMultipleMode is true`() {
        val actual =
            miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)

        assertTrue(actual)
    }

    @Test
    fun `onShowFileChooser should call onReceivedFiles should call resetCallback`() {
        val intent: Intent = mock()
        val uri: Uri = mock()
        whenever(intent.data).thenReturn(uri)
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(intent)
        verify(miniAppFileChooser).resetCallback()
    }

    @Test
    fun `onShowFileChooser should receiveValue onReceivedFiles when onReceivedFiles is called`() {
        val intent: Intent = mock()
        val uri: Uri = mock()
        whenever(intent.data).thenReturn(uri)
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(intent)
        verify(callback).onReceiveValue(arrayOf(uri))
    }

    @Test
    fun `onReceivedFiles should call resetCallback when type is ClipData`() {
        val intent: Intent = mock()
        val clipData: ClipData = mock()
        whenever(intent.clipData).thenReturn(clipData)
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(intent)
        verify(miniAppFileChooser).resetCallback()
    }

    @Test
    fun `onReceivedFiles should call onReceiveValue when type is ClipData`() {
        val intent: Intent = mock()
        val clipData: ClipData = mock()
        val uriList = mutableListOf<Uri>()
        whenever(intent.clipData).thenReturn(clipData)
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(intent)
        verify(callback).onReceiveValue(uriList.toTypedArray())
    }

    @Test
    fun `onReceivedFiles should call resetCallback when photo path is available`() {
        val intent: Intent = mock()
        miniAppFileChooser.currentPhotoPath = "test-photo-path"
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(intent)
        verify(miniAppFileChooser).resetCallback()
        verify(callback).onReceiveValue(any())
    }

    @Test
    fun `onReceivedFiles should call onReceiveValue when photo path is available`() {
        val intent: Intent = mock()
        miniAppFileChooser.currentPhotoPath = "test-photo-path"
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(intent)
        verify(callback).onReceiveValue(any())
    }

    @Test
    fun `onReceivedFiles should not invoke value when photo path is null`() {
        val intent: Intent = mock()
        miniAppFileChooser.currentPhotoPath = null
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(intent)
        verify(miniAppFileChooser).resetCallback()
    }

    @Test
    fun `onReceivedFiles should call resetCallback when no data in intent after onShowFileChooser`() {
        val intent: Intent = mock()
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(intent)
        verify(miniAppFileChooser).resetCallback()
    }

    @Test
    fun `onReceivedFiles should add null when no data in intent after onShowFileChooser`() {
        val intent: Intent = mock()
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(intent)
        verify(callback).onReceiveValue(null)
    }

    @Test
    fun `onReceivedFiles should not invoke onReceiveValue when file path callback is null`() {
        miniAppFileChooser.onShowFileChooser(null, fileChooserParams, context)
        verify(callback, times(0)).onReceiveValue(arrayOf())
    }

    @Test
    fun `onReceivedValue should add null to file path callback`() {
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onCancel()
        verify(callback).onReceiveValue(null)
    }

    @Test
    fun `onCancel should call resetCallback`() {
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onCancel()
        verify(miniAppFileChooser).resetCallback()
    }

    @Test
    fun `resetCallback should be null to appropriate properties`() {
        miniAppFileChooser.resetCallback()
        assertTrue(miniAppFileChooser.currentPhotoPath == null)
    }

    @Test
    fun `currentPhotoPath should be null to appropriate properties`() {
        miniAppFileChooser.resetCallback()
        assertTrue(miniAppFileChooser.callback == null)
    }

    @Test
    fun `extractValidMimeTypes should return the correct MimeType`() {
        val invalidMimeTypes = listOf(".jpg", ".jpeg", ".png", ".gif", ".pdf")
        val expectedMimeTypes = listOf(jpegMimeType, pngMimeType, gifMimeType, "application/pdf")
        assertEquals(
            expectedMimeTypes,
            miniAppFileChooser.extractValidMimeTypes(invalidMimeTypes.toTypedArray())
        )
    }

    @Test
    fun `extractValidMimeTypes should remove duplicate MimeType`() {
        val invalidMimeTypes = listOf(".jpg", ".jpeg", ".jpg")
        val expectedMimeTypes = listOf(jpegMimeType)
        assertEquals(
            expectedMimeTypes,
            miniAppFileChooser.extractValidMimeTypes(invalidMimeTypes.toTypedArray())
        )
    }

    @Test
    fun `extractValidMimeTypes should remove bad MimeType`() {
        val invalidMimeTypes = listOf(".jpg", ".badMime")
        val expectedMimeTypes = listOf(jpegMimeType)
        assertEquals(
            expectedMimeTypes,
            miniAppFileChooser.extractValidMimeTypes(invalidMimeTypes.toTypedArray())
        )
    }

    @Test
    fun `extractValidMimeTypes should not effect proper MimeType`() {
        val invalidMimeTypes = listOf(".png", jpegMimeType)
        val expectedMimeTypes = listOf(pngMimeType, jpegMimeType)
        assertEquals(
            expectedMimeTypes,
            miniAppFileChooser.extractValidMimeTypes(invalidMimeTypes.toTypedArray())
        )
    }

    @Test
    fun `createImageFile should prepare file correctly as expected`() {
        val actualFile = miniAppFileChooser.createImageFile(context)
        miniAppFileChooser.currentPhotoPath shouldBe actualFile.absolutePath
    }
}
