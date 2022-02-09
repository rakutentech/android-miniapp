package com.rakuten.tech.mobile.miniapp.js.hostenvironment

import android.app.Activity
import android.os.Build
import androidx.annotation.Keep
import com.rakuten.tech.mobile.miniapp.BuildConfig

/** HostEnvironmentInfo object for miniapp. */
@Keep
data class HostEnvironmentInfo constructor(
    val platformVersion: String,
    val hostVersion: String,
    val sdkVersion: String,
    val hostLocale: String
) {
    constructor(
        activity: Activity,
        hostLocale: String
    ) : this (
        platformVersion = Build.VERSION.RELEASE,
        hostVersion = activity.packageManager.getPackageInfo(
            activity.packageName, 0
        ).versionName,
        sdkVersion = BuildConfig.VERSION_NAME,
        hostLocale = hostLocale
    )
}
