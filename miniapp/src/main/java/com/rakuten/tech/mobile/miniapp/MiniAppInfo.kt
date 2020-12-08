package com.rakuten.tech.mobile.miniapp

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * This represents a Mini App entity.
 * @property id Mini App identifier unique to a mini app.
 * @property displayName Display name of the mini app.
 * @property icon Icon of the mini app, obtainable from the provided data for this resource.
 * @property version Version information of the mini app.
 */
@Parcelize
data class MiniAppInfo(
    // Must use @SerializedName on all properties for compatibility with Proguard obfuscation
    @SerializedName("id") val id: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("version") val version: Version
) : Parcelable {
    companion object {
        internal fun empty() = MiniAppInfo("", "", "", Version("", ""))
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
    @SerializedName("versionId") internal val versionId: String
) : Parcelable
