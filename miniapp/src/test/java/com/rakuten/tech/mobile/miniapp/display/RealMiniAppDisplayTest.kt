package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotHaveTheSameClassAs
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealMiniAppDisplayTest {

    lateinit var context: Context

    @Before
    fun setup() {
        context = getApplicationContext()
    }

    @Test
    fun `for a given basePath, obtainView returns corresponding view to the caller`() =
        runBlockingTest {
            val display = Displayer().createMiniAppDisplay(basePath = TEST_BASE_PATH)
            val miniAppView = display.obtainView(context)
            miniAppView shouldNotHaveTheSameClassAs WebView(context)
            if (miniAppView is WebView) {
                miniAppView.url shouldContain TEST_BASE_PATH
            }
        }

    @Test
    fun `for a given basePath, obtainView should not return WebView to the caller`() =
        runBlockingTest {
            val display = Displayer().createMiniAppDisplay(basePath = TEST_BASE_PATH)
            val miniAppView = display.obtainView(context)
            miniAppView shouldNotHaveTheSameClassAs WebView::class
        }
}
