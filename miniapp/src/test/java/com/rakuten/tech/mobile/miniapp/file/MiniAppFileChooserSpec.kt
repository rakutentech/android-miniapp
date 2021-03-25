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
class MiniAppFileChooserSpec {
    private lateinit var miniAppFileChooser: MiniAppFileChooser
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
            miniAppFileChooser = MiniAppFileChooser(requestCode)
        }
    }

    @Test
    fun `onShowFileChooser should assign filePathCallback correctly`() {
        miniAppFileChooser.onShowFileChooser(callback, fileChooserParams, context)
        assertEquals(miniAppFileChooser.callback, callback)
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
}
