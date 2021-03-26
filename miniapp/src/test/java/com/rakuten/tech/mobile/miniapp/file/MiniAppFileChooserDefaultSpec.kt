package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.miniapp.TestActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MiniAppFileChooserDefaultSpec {
    private lateinit var miniAppFileChooser: MiniAppFileChooserDefault
    private lateinit var context: Context
    private val requestCode = 100
    private val callback: ValueCallback<Array<Uri>>? = mock()
    private var fileChooserParams: WebChromeClient.FileChooserParams? = mock()
    private val intent: Intent = mock()

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            context = spy(activity)
            whenever(fileChooserParams?.createIntent()).thenReturn(intent)
            miniAppFileChooser = MiniAppFileChooserDefault(requestCode)
        }
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
    fun `onReceivedFiles should invoke onReceiveValue of file path callback correctly`() {
        val files: Array<Uri> = arrayOf()
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFileChooser.onReceivedFiles(files)
        verify(callback)?.onReceiveValue(files)
    }

    @Test
    fun `onReceivedFiles should not invoke onReceiveValue of file path callback is null`() {
        miniAppFileChooser.onShowFileChooser(null, fileChooserParams, context)
        verify(callback, times(0))?.onReceiveValue(arrayOf())
    }
}
