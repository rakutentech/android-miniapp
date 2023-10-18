//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.js.chat](../index.md)/[ChatBridgeDispatcher](index.md)/[sendMessageToMultipleContacts](send-message-to-multiple-contacts.md)

# sendMessageToMultipleContacts

[androidJvm]\
abstract fun [sendMessageToMultipleContacts](send-message-to-multiple-contacts.md)(message: [MessageToContact](../../com.rakuten.tech.mobile.miniapp.js/-message-to-contact/index.md), onSuccess: (contactIds: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;?) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), onError: (message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

Triggered when Mini App wants to send a message to multiple contacts. Should open a contact chooser which allows the user to choose multiple contacts, and should then send the message to all chosen contacts. Should invoke [onSuccess](send-message-to-multiple-contacts.md) with a list of IDs of the contacts which were successfully sent the message. If the user cancelled sending the message, should invoked [onSuccess](send-message-to-multiple-contacts.md) with null. Should invoke [onError](send-message-to-multiple-contacts.md) when there was an error.
