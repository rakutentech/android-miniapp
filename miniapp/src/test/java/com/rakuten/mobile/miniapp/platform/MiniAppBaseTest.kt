package com.rakuten.mobile.miniapp.platform

import org.junit.After
import org.mockito.Mockito

/**
 * Base test class of all tests in this module.
 */
open class MiniAppBaseTest {

    /** See [Memory leak in mockito-inline...](https://github.com/mockito/mockito/issues/1614)  */
    @After
    fun clearMocks() {
        Mockito.framework().clearInlineMocks()
    }

    companion object {
        const val DOWNLOAD_ENDPOINT = "miniapp/78d85043-d04f-486a-8212-bf2601cb63a2/version" +
                "/17bccee1-17f0-44fa-8cb8-2da89eb49905/manifest/"
    }
}
