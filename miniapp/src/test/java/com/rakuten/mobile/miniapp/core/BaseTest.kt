package com.rakuten.mobile.miniapp.core

import org.junit.After
import org.mockito.Mockito

/** Base test class of all test classes.  */
open class BaseTest {

    /** See [Memory leak in mockito-inline...](https://github.com/mockito/mockito/issues/1614)  */
    @After
    fun clearMocks() {
        Mockito.framework().clearInlineMocks()
    }

    companion object {
        const val VALID_MANIFEST_ENDPOINT =
            "miniapp/78d85043-d04f-486a-8212-bf2601cb63a2/version/17bccee1-17f0-44fa-8cb8" +
                    "-2da89eb49905/manifest/"

        const val INVALID_MANIFEST_ENDPOINT =
            "/78d85043-d04f-486a-8212-bf2601cb63a2/17bccee1-17f0-44fa-8cb8-2da89eb49905/manifest/"

        const val VALID_FILE_URL_PATH = "https://version/78d85043-d04f-486a-8212-bf2601cb63a2/js" +
                "/index.html"

        const val INVALID_FILE_URL_PATH = "https://78d85043-d04f-486a-8212-bf2601cb63a2/js"
    }
}
