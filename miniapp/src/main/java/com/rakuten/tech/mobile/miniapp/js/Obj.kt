package com.rakuten.tech.mobile.miniapp.js

import androidx.annotation.Keep
import com.rakuten.tech.mobile.miniapp.closealert.MiniAppCloseAlertInfo
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope

@Keep
internal data class CallbackObj(
    var action: String,
    var param: Any?,
    var id: String
)

@Keep
internal data class FileDownloadCallbackObj(
    var action: String,
    val param: FileDownloadParams?,
    var id: String
)

// region: Secure Storage
@Keep
internal data class SecureStorageCallbackObj(
    var action: String,
    val param: SecureStorageItems,
    var id: String
)

@Keep
internal data class SecureStorageItems(
    val secureStorageItems: Map<String, String>?
)

@Keep
internal data class DeleteItemsCallbackObj(
    var action: String,
    val param: SecureStorageKeyList?,
    var id: String
)

@Keep
internal data class SecureStorageKeyList(
    val secureStorageKeyList: Set<String>?
)

@Keep
internal data class GetItemCallbackObj(
    var action: String,
    val param: SecureStorageKey?,
    var id: String
)

@Keep
internal data class SecureStorageKey(
    val secureStorageKey: String
)

// end region

@Keep
internal data class DevicePermission(val permission: String)

@Keep
internal data class FileDownloadParams(
    val filename: String,
    val url: String,
    val headers: Map<String, String>
)

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
internal data class AccessTokenCallbackObj(
    var action: String,
    val param: AccessTokenScope?,
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

// close alert region
@Keep
internal data class CloseAlertInfoCallbackObj(val param: CloseAlertInfoParam?) {

    @Keep
    internal data class CloseAlertInfoParam(val closeAlertInfo: MiniAppCloseAlertInfo)
}

@Keep
internal data class CloseMiniAppCallbackObj(val withConfirmationAlert: Boolean)

// end region

// universal bridge region
@Keep
internal data class JsonInfo(val content: String)

@Keep
internal data class JsonInfoCallbackObj(val param: JsonInfoParam) {

    @Keep
    internal data class JsonInfoParam(val jsonInfo: JsonInfo)
}
// end region

// In-App Purchase region
@Keep
internal data class PurchasedProductCallbackObj(
    val action: String,
    val param: ProductItem,
    var id: String
) {

    @Keep
    internal data class ProductItem(
        val productId: String
    )
}

@Keep
internal data class ConsumePurchaseCallbackObj(
    val action: String,
    val param: Purchase,
    var id: String
) {

    @Keep
    internal data class Purchase(
        val productId: String,
        val productTransactionId: String,
    )
}
// end region
