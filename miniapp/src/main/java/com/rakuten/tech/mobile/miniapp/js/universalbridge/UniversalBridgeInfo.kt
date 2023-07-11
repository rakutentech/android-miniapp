package com.rakuten.tech.mobile.miniapp.js.universalbridge

import androidx.annotation.Keep

/** An object that is used for sharing Info from Miniapp to Host application*/
@Keep
data class UniversalBridgeInfo(
    var key: String = "",
    var value: String = "",
    var description: String = ""
)
