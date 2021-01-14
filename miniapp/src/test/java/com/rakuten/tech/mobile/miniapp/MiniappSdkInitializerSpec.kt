package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiniappSdkInitializerSpec {
    private val miniappSdkInitializer = MiniappSdkInitializer()
    private lateinit var context: Context

    @Before
    fun setup() {
        context = getApplicationContext()
    }

    @Test
    fun `The MiniappSdkInitializer should be overriden from ContentProvider`() {
        miniappSdkInitializer.onCreate()
        val uri: Uri = mock()
        miniappSdkInitializer.query(
            uri, null, null, null, null
        ) shouldBe null
        miniappSdkInitializer.update(
            uri, null, null, null
        ) shouldBe 0
        miniappSdkInitializer.insert(uri, null) shouldBe null
        miniappSdkInitializer.delete(
            uri, null, null
        ) shouldBe 0
        miniappSdkInitializer.getType(uri) shouldBe null
    }
}
