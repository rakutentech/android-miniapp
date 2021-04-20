package com.rakuten.tech.mobile.testapp.ui.chat

import androidx.annotation.Keep
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact

@Keep
data class SelectableContact(
    val contact: Contact,
    var isSelected: Boolean = false
)
