package com.rakuten.tech.mobile.miniapp.display

import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test

class DisplayerTest {

    @Test
    fun `for a given base path createMiniAppDisplay returns an implementer of MiniAppDisplay`() =
        runBlockingTest {
            val obtainedDisplay = Displayer().createMiniAppDisplay(basePath = TEST_BASE_PATH)
            obtainedDisplay shouldBeInstanceOf RealMiniAppDisplay::class
        }
}
