package com.rakuten.tech.mobile.miniapp.js

import androidx.annotation.Keep

/** An object to prepare the message for sending to contacts. */
@Keep
data class MessageToContact(
    val image: String,
    val text: String,
    val caption: String,
    val action: String
) {
    /** Returns true when all the properties are empty, otherwise returns false. */
    val isEmpty: Boolean =
        image.isEmpty() && text.isEmpty() && caption.isEmpty() && action.isEmpty()
}
