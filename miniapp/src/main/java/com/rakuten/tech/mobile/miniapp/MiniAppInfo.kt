package com.rakuten.tech.mobile.miniapp

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.UUID

/**
 * This represents a Mini App entity.
 * @property id Mini App identifier unique to a mini app.
 * @property displayName Display name of the mini app.
 * @property icon Icon of the mini app, obtainable from the provided data for this resource.
 * @property version Version information of the mini app.
 * @property promotionalImageUrl promotional image, obtainable from the provided data for this resource.
 * @property promotionalText promotional details to share.
 */
@Parcelize
data class MiniAppInfo(
    // Must use @SerializedName on all properties for compatibility with Proguard obfuscation
    @SerializedName("id") val id: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("version") val version: Version,
    @SerializedName("promotionalImageUrl") val promotionalImageUrl: String,
    @SerializedName("promotionalText") val promotionalText: String
) : Parcelable {
    companion object {
        internal fun forUrl() =
            MiniAppInfo(UUID.randomUUID().toString(), "", "", Version("", ""), "", "")
    }
}

/**
 * This represents a version entity of a Mini App.
 * @property versionTag Version information of the mini app.
 * @property versionId Version identifier of the mini app.
 */
@Parcelize
data class Version(
    @SerializedName("versionTag") val versionTag: String,
    @SerializedName("versionId") val versionId: String
) : Parcelable

/**
 * This represents a host entity of a Mini App.
 * @property id host id information of the mini app.
 * @property subscriptionKey subscription identifier of the mini app.
 */
@Parcelize
data class Host(
    @SerializedName("id") val id: String,
    @SerializedName("subscriptionKey") val subscriptionKey: String
) : Parcelable

/**
 * This represents a response entity for preview code.
 * @property host host identifier unique to a mini app.
 * @property miniapp represents a Mini App entity.
 */
@Parcelize
data class PreviewMiniAppInfo(
    @SerializedName("host") val host: Host,
    @SerializedName("miniapp") val miniapp: MiniAppInfo
) : Parcelable
