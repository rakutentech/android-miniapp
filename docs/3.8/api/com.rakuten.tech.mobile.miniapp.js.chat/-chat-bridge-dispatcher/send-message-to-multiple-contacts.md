[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.js.chat](../index.md) / [ChatBridgeDispatcher](index.md) / [sendMessageToMultipleContacts](./send-message-to-multiple-contacts.md)

# sendMessageToMultipleContacts

`abstract fun sendMessageToMultipleContacts(message: `[`MessageToContact`](../../com.rakuten.tech.mobile.miniapp.js/-message-to-contact/index.md)`, onSuccess: (contactIds: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>?) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onError: (message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Triggered when Mini App wants to send a message to multiple contacts.
Should open a contact chooser which allows the user to choose multiple contacts,
and should then send the message to all chosen contacts.
Should invoke [onSuccess](send-message-to-multiple-contacts.md#com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher$sendMessageToMultipleContacts(com.rakuten.tech.mobile.miniapp.js.MessageToContact, kotlin.Function1((kotlin.collections.List((kotlin.String)), kotlin.Unit)), kotlin.Function1((kotlin.String, kotlin.Unit)))/onSuccess) with a list of IDs of the contacts which were successfully sent the message.
If the user cancelled sending the message, should invoked [onSuccess](send-message-to-multiple-contacts.md#com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher$sendMessageToMultipleContacts(com.rakuten.tech.mobile.miniapp.js.MessageToContact, kotlin.Function1((kotlin.collections.List((kotlin.String)), kotlin.Unit)), kotlin.Function1((kotlin.String, kotlin.Unit)))/onSuccess) with null.
Should invoke [onError](send-message-to-multiple-contacts.md#com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher$sendMessageToMultipleContacts(com.rakuten.tech.mobile.miniapp.js.MessageToContact, kotlin.Function1((kotlin.collections.List((kotlin.String)), kotlin.Unit)), kotlin.Function1((kotlin.String, kotlin.Unit)))/onError) when there was an error.

