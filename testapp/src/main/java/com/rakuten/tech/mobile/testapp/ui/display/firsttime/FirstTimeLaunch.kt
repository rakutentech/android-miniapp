package com.rakuten.tech.mobile.testapp.ui.display.firsttime

import androidx.annotation.Keep

@Keep
data class FirstTimeLaunch(
    val appId: String,
    val isFirstTime: Boolean
)
