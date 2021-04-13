package com.rakuten.tech.mobile.miniapp.js

import androidx.annotation.Keep

@Keep
internal data class CallbackObj(
    var action: String,
    var param: Any?,
    var id: String
)

@Keep
internal data class DevicePermission(val permission: String)

@Keep
internal data class Screen(val action: String)

// custom permission region
@Keep
internal data class CustomPermissionCallbackObj(
    var action: String,
    val param: CustomPermission?,
    var id: String
)

@Keep
internal data class CustomPermission(
    val permissions: List<CustomPermissionObj>
)

@Keep
internal data class CustomPermissionObj(
    val name: String,
    val description: String
)
// end region

// shared info region
@Keep
internal data class ShareInfo(val content: String)

@Keep
internal data class ShareInfoCallbackObj(val param: ShareInfoParam) {

    @Keep
    internal data class ShareInfoParam(val shareInfo: ShareInfo)
}
// end region

// Ad region
@Keep
internal data class AdObj(
    val adType: Int,
    val adUnitId: String
)

@Keep
internal data class AdCallbackObj(val param: AdObj)
// end region

// action response region
internal const val SUCCESS = "success"
internal const val CLOSED = "closed"
// end region

// send contact region
@Keep
internal data class SendContactCallbackObj(val param: MessageParam) {

    @Keep
    internal data class MessageParam(val messageToContact: MessageToContact)
}

@Keep
internal data class SendContactIdCallbackObj(val param: MessageParamId) {

    @Keep
    internal data class MessageParamId(
        val contactId: String,
        val messageToContact: MessageToContact
    )
}
// end region
