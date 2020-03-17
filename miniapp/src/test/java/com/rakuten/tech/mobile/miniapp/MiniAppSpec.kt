package com.rakuten.tech.mobile.miniapp

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class MiniAppSpec {

    @Test
    fun `should update configuration when get instance of MiniApp`() {
        val miniApp: MiniApp = mock()
        val miniAppSdkConfig: MiniAppSdkConfig = mock()
        val miniAppCompanion = MiniApp.Companion

        miniAppCompanion.instance = miniApp
        miniAppCompanion.instance(miniAppSdkConfig)

        verify(miniApp, times(1)).updateConfiguration(miniAppSdkConfig)
    }
}
