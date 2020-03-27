package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppMessageInterface
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayerTest {

    private lateinit var context: Context
    private val miniAppMessageInterface: MiniAppMessageInterface = mock()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `for a given base path createMiniAppDisplay returns an implementer of MiniAppDisplay`() =
        runBlockingTest {
            val obtainedDisplay = getMiniAppDisplay()
            obtainedDisplay shouldBeInstanceOf RealMiniAppDisplay::class
        }

    @Test
    fun `for a given base path createMiniAppDisplay returns an implementer of LifecycleObserver`() =
        runBlockingTest {
            val obtainedDisplay = getMiniAppDisplay()
            obtainedDisplay shouldBeInstanceOf LifecycleObserver::class
        }

    private suspend fun getMiniAppDisplay(): MiniAppDisplay =
        Displayer(context).createMiniAppDisplay(
            basePath = context.filesDir.path,
            appId = TEST_MA_ID,
            miniAppMessageInterface = miniAppMessageInterface
        )
}
