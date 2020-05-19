package com.rakuten.tech.mobile.miniapp

import android.os.Parcelable
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
    val id: String,
    val displayName: String,
    val icon: String,
    val version: Version
) : Parcelable

/**
 * This represents a version entity of a Mini App.
 * @property versionTag Version information of the mini app.
 * @property versionId Version identifier of the mini app.
 */
@Parcelize
data class Version(
    val versionTag: String,
    internal val versionId: String
) : Parcelable
