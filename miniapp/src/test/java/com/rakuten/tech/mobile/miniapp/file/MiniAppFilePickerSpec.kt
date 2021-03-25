package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nhaarman.mockitokotlin2.mock
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.rakuten.tech.mobile.miniapp.TestActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MiniAppFilePickerSpec {
    private lateinit var miniAppFilePicker: MiniAppFilePicker
    private lateinit var context: Context
    private val requestCode = 100
    private val callback: ValueCallback<Array<Uri>>? = mock()
    private val fileChooserParams: WebChromeClient.FileChooserParams?? = mock()
    private val intent: Intent = mock()

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            context = spy(activity)
            whenever(fileChooserParams?.createIntent()).thenReturn(intent)
            miniAppFilePicker = MiniAppFilePicker(requestCode)
        }
    }

    @Test
    fun `onShowFileChooser should assign filePathCallback correctly`() {
        miniAppFilePicker.onShowFileChooser(callback, fileChooserParams, context)
        assertEquals(miniAppFilePicker.callback, callback)
    }

    @Test
    fun `onShowFileChooser should startActivityForResult correctly`() {
        val actual = miniAppFilePicker.onShowFileChooser(callback, fileChooserParams, context)
        verify(context as Activity).startActivityForResult(intent, requestCode)
        assertTrue(actual)
    }

    @Test
    fun `onShowFileChooser should return false when there is exception`() {
        val mockContext: Context = mock()
        val actual = miniAppFilePicker.onShowFileChooser(mock(), mock(), mockContext)
        assertFalse(actual)
    }

    @Test
    fun `onReceivedFiles should invoke onReceiveValue of file path callback correctly`() {
        val files: Array<Uri> = arrayOf()
        miniAppFilePicker.onShowFileChooser(callback, fileChooserParams, context)
        miniAppFilePicker.onReceivedFiles(files)
        verify(callback)?.onReceiveValue(files)
    }
}
