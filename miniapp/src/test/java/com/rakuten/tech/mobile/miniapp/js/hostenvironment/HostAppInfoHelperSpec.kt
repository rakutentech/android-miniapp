package com.rakuten.tech.mobile.miniapp.js.hostenvironment

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HostAppInfoHelperSpec {

    @Test
    fun `host app locale value should match with appropriate pattern`() {
        assertTrue("en-US".isValidLocale())
    }

    @Test
    fun `host app locale value should not match with inappropriate pattern`() {
        assertFalse("en_US".isValidLocale())
        assertFalse("00-00".isValidLocale())
        assertFalse("enn-US".isValidLocale())
        assertFalse("en-USS".isValidLocale())
        assertFalse("enn-USS".isValidLocale())
    }
}
