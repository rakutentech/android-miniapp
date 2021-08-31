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
    private val ratDispatcher: MessageBridgeRatDispatcher = MessageBridgeRatDispatcher(
        miniAppAnalytics
    )
    private val testActionValues = mapOf<String, Actype>(
        ActionType.REQUEST_PERMISSION.action to Actype.REQUEST_PERMISSION,
        ActionType.REQUEST_CUSTOM_PERMISSIONS.action to Actype.REQUEST_CUSTOM_PERMISSIONS,
        ActionType.SHARE_INFO.action to Actype.SHARE_INFO,
        ActionType.LOAD_AD.action to Actype.LOAD_AD,
        ActionType.SHOW_AD.action to Actype.SHOW_AD,
        ActionType.GET_USER_NAME.action to Actype.GET_USER_NAME,
        ActionType.GET_PROFILE_PHOTO.action to Actype.GET_PROFILE_PHOTO,
        ActionType.GET_ACCESS_TOKEN.action to Actype.GET_ACCESS_TOKEN,
        ActionType.SET_SCREEN_ORIENTATION.action to Actype.SET_SCREEN_ORIENTATION,
        ActionType.GET_CONTACTS.action to Actype.GET_CONTACTS,
        ActionType.SEND_MESSAGE_TO_CONTACT.action to Actype.SEND_MESSAGE_TO_CONTACT,
        ActionType.SEND_MESSAGE_TO_CONTACT_ID.action to Actype.SEND_MESSAGE_TO_CONTACT_ID,
        ActionType.SEND_MESSAGE_TO_MULTIPLE_CONTACTS.action to Actype.SEND_MESSAGE_TO_MULTIPLE_CONTACTS,
        ActionType.GET_POINTS.action to Actype.GET_POINTS
    )

    @Test
    fun `should send analytics with correct params`() = runBlockingTest {
        for (entry in testActionValues.entries) {
            ratDispatcher.sendAnalyticsSdkFeature(entry.key)
            verify(miniAppAnalytics, times(1)).sendAnalytics(
                eType = Etype.CLICK,
                actype = ratDispatcher.getAcType(entry.key),
                miniAppInfo = null
            )
        }
    }

    @Test
    fun `getAcType should return the correct acType for correct action`() {
        for (entry in testActionValues.entries) {
            val acType = ratDispatcher.getAcType(entry.key)
            assertEquals(expected = entry.value, actual = acType)
        }
    }

    @Test
    fun `getAcType should return the default acType for wrong action`() {
        val acType = ratDispatcher.getAcType("")
        assertEquals(expected = Actype.DEFAULT, actual = acType)
    }
}
