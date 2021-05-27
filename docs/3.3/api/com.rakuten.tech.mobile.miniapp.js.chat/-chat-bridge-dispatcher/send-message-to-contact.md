[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.js.chat](../index.md) / [ChatBridgeDispatcher](index.md) / [sendMessageToContact](./send-message-to-contact.md)

# sendMessageToContact

`abstract fun sendMessageToContact(message: `[`MessageToContact`](../../com.rakuten.tech.mobile.miniapp.js/-message-to-contact/index.md)`, onSuccess: (contactId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onError: (message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Triggered when Mini App wants to send a message to a single contact.
Should invoke [onSuccess](send-message-to-contact.md#com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher$sendMessageToContact(com.rakuten.tech.mobile.miniapp.js.MessageToContact, kotlin.Function1((kotlin.String, kotlin.Unit)), kotlin.Function1((kotlin.String, kotlin.Unit)))/onSuccess) with the contact ID to send the message.
If the user wants to cancel sending the message, should invoked [onSuccess](send-message-to-contact.md#com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher$sendMessageToContact(com.rakuten.tech.mobile.miniapp.js.MessageToContact, kotlin.Function1((kotlin.String, kotlin.Unit)), kotlin.Function1((kotlin.String, kotlin.Unit)))/onSuccess) with null.
Should invoke [onError](send-message-to-contact.md#com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher$sendMessageToContact(com.rakuten.tech.mobile.miniapp.js.MessageToContact, kotlin.Function1((kotlin.String, kotlin.Unit)), kotlin.Function1((kotlin.String, kotlin.Unit)))/onError) when there was an error.

