package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.Keep

@Keep
data class MessageToContact(
    val image: String,
    val text: String,
    val caption: String,
    val action: String,
    val title: String
) {
    val isEmpty =
        image.isEmpty() && text.isEmpty() && caption.isEmpty() && action.isEmpty() && title.isEmpty()
}
