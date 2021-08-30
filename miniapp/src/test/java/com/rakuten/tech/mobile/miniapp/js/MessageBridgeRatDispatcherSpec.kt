package com.rakuten.tech.mobile.miniapp.js

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.analytics.Actype
import com.rakuten.tech.mobile.miniapp.analytics.Etype
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MessageBridgeRatDispatcherSpec {

    private var miniAppAnalytics: MiniAppAnalytics = mock()
    private val ratDispatcher: MessageBridgeRatDispatcher = MessageBridgeRatDispatcher(miniAppAnalytics)
    private val testAction = ActionType.GET_ACCESS_TOKEN.action

    @Test
    fun `should send analytics with correct params`() = runBlockingTest {
        ratDispatcher.sendAnalyticsSdkFeature(testAction)
        verify(miniAppAnalytics, times(1)).sendAnalytics(
            eType = Etype.CLICK,
            actype = ratDispatcher.getAcType(testAction),
            miniAppInfo = null
        )
    }

    @Test
    fun `getAcType should return the correct acType for correct action`() {
        val acType = ratDispatcher.getAcType(testAction)
        assertEquals(expected = Actype.GET_ACCESS_TOKEN, actual = acType)
    }

    @Test
    fun `getAcType should return the default acType for wrong action`() {
        val acType = ratDispatcher.getAcType("")
        assertEquals(expected = Actype.DEFAULT, actual = acType)
    }
}
