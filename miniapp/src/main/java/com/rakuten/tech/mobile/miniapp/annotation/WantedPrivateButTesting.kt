package com.rakuten.tech.mobile.miniapp.annotation

import androidx.annotation.VisibleForTesting

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@VisibleForTesting
internal annotation class WantedPrivateButTesting
