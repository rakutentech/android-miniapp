package com.rakuten.tech.mobile.miniapp

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class MiniAppSpec {

    @Test
    fun `should update configuration when get instance of MiniApp`() {
        val miniAppSdkConfig: MiniAppSdkConfig = mock()
        val miniAppCompanion: MiniApp.Companion = MiniApp.Companion
        val miniApp: MiniApp = mock()

        val instance = MiniApp.Companion::class.memberProperties.first { it.name == "instance" }
        instance.isAccessible = true
        if (instance is KMutableProperty<*>)
            instance.setter.call(MiniApp.Companion, miniApp)

        miniAppCompanion.instance(miniAppSdkConfig)

        verify(miniApp, times(1)).updateConfiguration(miniAppSdkConfig)
    }
}
