package com.rakuten.tech.mobile.miniapp

import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test
import java.lang.Exception

class MiniAppSdkExceptionSpec {

    @Test
    fun `should be instance of MiniAppSdkException`() {
        val exceptionClass = MiniAppSdkException(Exception())::class.java

        MiniAppNetException(Exception()) shouldBeInstanceOf exceptionClass
        sdkExceptionForInternalServerError() shouldBeInstanceOf exceptionClass
        sdkExceptionForInvalidArguments() shouldBeInstanceOf exceptionClass
        sdkExceptionForInvalidVersion() shouldBeInstanceOf exceptionClass
    }
}
