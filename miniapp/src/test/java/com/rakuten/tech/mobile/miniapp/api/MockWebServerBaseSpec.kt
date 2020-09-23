package com.rakuten.tech.mobile.miniapp.api

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import java.util.logging.Level
import java.util.logging.LogManager

open class MockWebServerBaseSpec(private val mockServer: MockWebServer) {
    private lateinit var baseUrl: String

    init {
        LogManager.getLogManager()
            .getLogger(MockWebServer::class.java.name).level = Level.OFF
    }

    @Before
    fun mockServerSetup() {
        mockServer.start()
        baseUrl = mockServer.url("/").toString()
    }

    @After
    fun mockServerTeardown() {
        mockServer.shutdown()
    }
}
