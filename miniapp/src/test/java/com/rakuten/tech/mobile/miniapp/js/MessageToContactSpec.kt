package com.rakuten.tech.mobile.miniapp.js

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MessageToContactSpec {

    @Test
    fun `should return true when all properties are empty in MessageToContact object`() {
        val emptyMessage = MessageToContact("", "", "", "")
        assertTrue(emptyMessage.isEmpty)
    }

    @Test
    fun `should return false when all properties are empty in MessageToContact object`() {
        val message = MessageToContact("dummyImage", "dummyText", "", "")
        assertFalse(message.isEmpty)
    }
}
