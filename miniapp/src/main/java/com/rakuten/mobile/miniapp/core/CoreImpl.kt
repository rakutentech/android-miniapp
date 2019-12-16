package com.rakuten.mobile.miniapp.core

import android.annotation.SuppressLint
import android.content.Context

/**
 * Base module which holds host app's application context.
 */
class CoreImpl {

    companion object {
        /**
         * Host App's application context.
         */
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        var context: Context? = null
    }
}
