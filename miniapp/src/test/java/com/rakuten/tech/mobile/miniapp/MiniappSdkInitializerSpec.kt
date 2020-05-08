package com.rakuten.tech.mobile.miniapp

import android.net.Uri
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldBe
import org.junit.Test

class MiniappSdkInitializerSpec {
    private val miniappSdkInitializer = MiniappSdkInitializer()

    @Test
    fun `The MiniappSdkInitializer should be overriden from ContentProvider`() {
        miniappSdkInitializer.onCreate()
        val uri: Uri = mock()
        miniappSdkInitializer.query(
            uri,null, null, null, null) shouldBe null
        miniappSdkInitializer.update(
            uri, null, null, null) shouldBe 0
        miniappSdkInitializer.insert(uri, null) shouldBe null
        miniappSdkInitializer.delete(
            uri, null, null) shouldBe 0
        miniappSdkInitializer.getType(uri) shouldBe null
    }
}
