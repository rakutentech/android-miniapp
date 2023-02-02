package com.rakuten.tech.mobile.miniapp.closealert

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiniAppCloseAlertInfoSpec {

    @Test
    fun `default values of MiniAppCloseAlertInfo should be expected`() {
        val closeAlertInfo = MiniAppCloseAlertInfo()
        assertFalse { closeAlertInfo.shouldDisplay }
        assertTrue { closeAlertInfo.title == "" }
        assertTrue { closeAlertInfo.description == "" }
    }
}
