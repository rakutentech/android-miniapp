package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayerTest {

    lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `for a given base path createMiniAppDisplay returns an implementer of MiniAppDisplay`() =
        runBlockingTest {
            val obtainedDisplay = Displayer(context).createMiniAppDisplay(
                basePath = context.filesDir.path,
                appId = TEST_MA_ID
            )
            obtainedDisplay shouldBeInstanceOf RealMiniAppDisplay::class
        }
}
