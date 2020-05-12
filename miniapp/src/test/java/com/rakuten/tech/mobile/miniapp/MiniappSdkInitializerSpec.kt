package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.rakuten.tech.mobile.sdkutils.AppInfo

@RunWith(AndroidJUnit4::class)
class MiniappSdkInitializerSpec {
    private val miniappSdkInitializer = MiniappSdkInitializer()
    private lateinit var context: Context

    @Before
    fun setup() {
        context = getApplicationContext()
    }

    @Test
    fun `Miniapp should be initialized`() {
        val sdkInitializer = Mockito.spy(miniappSdkInitializer)
        val appManifestConfig: AppManifestConfig = mock()
        AppInfo.instance = mock()

        When calling sdkInitializer.context itReturns context
        When calling sdkInitializer.createAppManifestConfig(context) itReturns appManifestConfig
        When calling appManifestConfig.baseUrl() itReturns TEST_URL_HTTPS_2
        When calling appManifestConfig.rasAppId() itReturns TEST_HA_ID_APP
        When calling appManifestConfig.subscriptionKey() itReturns TEST_HA_SUBSCRIPTION_KEY
        When calling appManifestConfig.hostAppVersion() itReturns TEST_HA_ID_VERSION

        sdkInitializer.onCreate() shouldBe true
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
