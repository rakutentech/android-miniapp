package com.rakuten.tech.mobile.miniapp.js.chat

import com.google.gson.Gson
import org.mockito.kotlin.*
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_VALUE
import com.rakuten.tech.mobile.miniapp.TEST_ERROR_MSG
import com.rakuten.tech.mobile.miniapp.TEST_MA
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.*
import com.rakuten.tech.mobile.miniapp.js.chat.ChatBridge.Companion.ERR_SEND_MESSAGE
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

open class BaseChatBridgeDispatcherSpec {
    internal lateinit var miniAppBridge: MiniAppMessageBridge
    internal val singleChatCallbackObj = CallbackObj(
        action = ActionType.SEND_MESSAGE_TO_CONTACT.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    internal val multipleChatCallbackObj = CallbackObj(
        action = ActionType.SEND_MESSAGE_TO_MULTIPLE_CONTACTS.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    internal val specificIdCallbackObj = CallbackObj(
        action = ActionType.SEND_MESSAGE_TO_CONTACT_ID.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    internal val customPermissionCache: MiniAppCustomPermissionCache = mock()
    internal val downloadedManifestCache: DownloadedManifestCache = mock()
    internal val webViewListener: WebViewListener = mock()
    internal val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))

    @Before
    fun setup() {
        miniAppBridge = Mockito.spy(createMessageBridge())
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = customPermissionCache,
            downloadedManifestCache = downloadedManifestCache,
            miniAppId = TEST_MA.id
        )
    }

    protected fun createChatMessageBridgeDispatcher(
        canSendMessage: Boolean,
        isCancelSingleOp: Boolean = false,
        isCancelContactIdOp: Boolean = false,
        isDifferentContactIdOp: Boolean = false,
        isCancelMultipleOp: Boolean = false
    ): ChatBridgeDispatcher = object : ChatBridgeDispatcher {

        override fun sendMessageToContact(
            message: MessageToContact,
            onSuccess: (contactId: String?) -> Unit,
            onError: (message: String) -> Unit
        ) {
            when {
                isCancelSingleOp -> onSuccess.invoke(null)
                canSendMessage -> onSuccess.invoke(TEST_CONTACT.id)
                else -> onError.invoke(TEST_ERROR_MSG)
            }
        }

        override fun sendMessageToContactId(
            contactId: String,
            message: MessageToContact,
            onSuccess: (contactId: String?) -> Unit,
            onError: (message: String) -> Unit
        ) {
            when {
                isCancelContactIdOp -> onSuccess.invoke(null)
                canSendMessage -> onSuccess.invoke(TEST_CONTACT.id)
                isDifferentContactIdOp -> onSuccess.invoke("different_contact_id")
                else -> onError.invoke(TEST_ERROR_MSG)
            }
        }

        override fun sendMessageToMultipleContacts(
            message: MessageToContact,
            onSuccess: (contactIds: List<String>?) -> Unit,
            onError: (message: String) -> Unit
        ) {
            when {
                isCancelMultipleOp -> onSuccess.invoke(emptyList())
                canSendMessage -> onSuccess.invoke(listOf(TEST_CONTACT.id))
                else -> onError.invoke(TEST_ERROR_MSG)
            }
        }
    }

    protected val messageToContact = MessageToContact("", "", "", "")

    internal fun createChatBridge(
        chatBridgeDispatcher: ChatBridgeDispatcher,
        isImplement: Boolean
    ): ChatBridge {
        val chatBridge = ChatBridge()
        chatBridge.setMiniAppComponents(bridgeExecutor, TEST_MA.id)
        if (isImplement)
            chatBridge.setChatBridgeDispatcher(chatBridgeDispatcher)

        return chatBridge
    }

    protected fun createMessageBridge(): MiniAppMessageBridge =
        object : MiniAppMessageBridge() {
            override fun getUniqueId() = TEST_CALLBACK_VALUE
        }

    protected val sendingMessageJsonStr = Gson().toJson(
        CallbackObj(
            action = ActionType.SEND_MESSAGE_TO_CONTACT.action,
            param = SendContactCallbackObj.MessageParam(messageToContact),
            id = TEST_CALLBACK_ID
        )
    )

    protected val multipleMessageJsonStr = Gson().toJson(
        CallbackObj(
            action = ActionType.SEND_MESSAGE_TO_MULTIPLE_CONTACTS.action,
            param = SendContactCallbackObj.MessageParam(messageToContact),
            id = TEST_CALLBACK_ID
        )
    )

    protected val specificMessageJsonStr = Gson().toJson(
        CallbackObj(
            action = ActionType.SEND_MESSAGE_TO_CONTACT_ID.action,
            param = SendContactIdCallbackObj.MessageParamId("contactId", messageToContact),
            id = TEST_CALLBACK_ID
        )
    )

    internal val chatBridgeOnSuccess = Mockito.spy(
        createChatBridge(
            Mockito.spy(
                createChatMessageBridgeDispatcher(true)
            ), true
        )
    )
}

class ChatBridgeDispatcherSpec : BaseChatBridgeDispatcherSpec() {
    @Test
    fun `postError should be called when hostapp doesn't implement the chat dispatcher for single chat`() {
        val chatMessageBridgeDispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, false))
        chatBridge.onSendMessageToContact(singleChatCallbackObj.id, sendingMessageJsonStr)

        verify(bridgeExecutor).postError(singleChatCallbackObj.id, ErrorBridgeMessage.NO_IMPL)
    }

    @Test
    fun `postError should be called when hostapp doesn't implement the chat dispatcher for multiple chat`() {
        val chatMessageBridgeDispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, false))
        chatBridge.onSendMessageToMultipleContacts(multipleChatCallbackObj.id, multipleMessageJsonStr)

        verify(bridgeExecutor).postError(multipleChatCallbackObj.id, ErrorBridgeMessage.NO_IMPL)
    }

    @Test
    fun `postError should be called when hostapp doesn't implement the chat dispatcher`() {
        val chatMessageBridgeDispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, false))
        chatBridge.onSendMessageToContact(specificIdCallbackObj.id, sendingMessageJsonStr)

        verify(bridgeExecutor).postError(specificIdCallbackObj.id, ErrorBridgeMessage.NO_IMPL)
    }

    @Test
    fun `postError should be called when hostapp can't send message`() {
        val chatMessageBridgeDispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, true))
        val errMsg = "$ERR_SEND_MESSAGE $TEST_ERROR_MSG"
        chatBridge.onSendMessageToContact(singleChatCallbackObj.id, sendingMessageJsonStr)

        verify(bridgeExecutor).postError(singleChatCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp can't send message to multiple contacts`() {
        val chatMessageBridgeDispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, true))
        val errMsg = "$ERR_SEND_MESSAGE $TEST_ERROR_MSG"

        chatBridge.onSendMessageToContact(multipleChatCallbackObj.id, multipleMessageJsonStr)
        verify(bridgeExecutor).postError(multipleChatCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp can't send message to a contact id`() {
        val chatMessageBridgeDispatcher =
            Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, true))
        val errMsg = "$ERR_SEND_MESSAGE $TEST_ERROR_MSG"

        chatBridge.onSendMessageToContact(specificIdCallbackObj.id, specificMessageJsonStr)
        verify(bridgeExecutor).postError(specificIdCallbackObj.id, errMsg)
    }

    /** end region */

    /** region: onSuccess with value */
    @Test
    fun `postValue should be called when hostapp can send message to single contact`() {
        chatBridgeOnSuccess.onSendMessageToContact(singleChatCallbackObj.id, sendingMessageJsonStr)
        verify(bridgeExecutor).postValue(singleChatCallbackObj.id, TEST_CONTACT.id)
    }

    @Test
    fun `postValue should be called when hostapp can send message to specific contact id`() {
        chatBridgeOnSuccess.onSendMessageToContact(specificIdCallbackObj.id, specificMessageJsonStr)
        verify(bridgeExecutor).postValue(specificIdCallbackObj.id, TEST_CONTACT.id)
    }

    @Test
    fun `postValue should be called when hostapp can send message to multiple contacts`() {
        chatBridgeOnSuccess.onSendMessageToMultipleContacts(
            multipleChatCallbackObj.id, multipleMessageJsonStr
        )
        verify(bridgeExecutor).postValue(
            multipleChatCallbackObj.id,
            Gson().toJson(listOf(TEST_CONTACT.id)).toString()
        )
    }

    /** end region */

    /** region: onSuccess with null when cancellation */
    @Test
    fun `postValue should be called when hostapp wants to cancel sending message to single contact`() {
        val dispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false, true, false, false, false))
        val chatBridge = Mockito.spy(createChatBridge(dispatcher, true))
        chatBridge.onSendMessageToContact(singleChatCallbackObj.id, sendingMessageJsonStr)
        verify(bridgeExecutor).postValue(singleChatCallbackObj.id, "null")
    }

    @Test
    fun `postValue should be called when hostapp wants to cancel sending message to specific contact id`() {
        val dispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false, false, true, false, false))
        val chatBridge = Mockito.spy(createChatBridge(dispatcher, true))
        chatBridge.onSendMessageToContactId(specificIdCallbackObj.id, specificMessageJsonStr)
        verify(bridgeExecutor).postValue(specificIdCallbackObj.id, "null")
    }

    @Test
    fun `postValue should be called when hostapp wants to cancel sending message to multiple contacts`() {
        val dispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false, false, false, false, true))
        val chatBridge = Mockito.spy(createChatBridge(dispatcher, true))
        chatBridge.onSendMessageToMultipleContacts(multipleChatCallbackObj.id, multipleMessageJsonStr)
        verify(bridgeExecutor).postValue(multipleChatCallbackObj.id, "null")
    }

    /** end region */

    @Test
    fun `postValue should be called with null when hostapp wants to send a different specific contact id`() {
        val dispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false, false, false, true, false))
        val chatBridge = Mockito.spy(createChatBridge(dispatcher, true))
        chatBridge.onSendMessageToContactId(specificIdCallbackObj.id, specificMessageJsonStr)
        verify(bridgeExecutor).postValue(specificIdCallbackObj.id, "null")
    }
}
