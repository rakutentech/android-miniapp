package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * This provides navigation using MiniApp Demo Scheme that doesn't require Camera Permission from
 * another app
 */
class SchemeByUrlActivity : SchemeActivity() {
    companion object {
        fun start(
            context: Context,
            miniAppUrl: String
        ) {
            context.startActivity(Intent(context, SchemeByUrlActivity::class.java).apply {
                data = Uri.parse(miniAppUrl)
            })
        }
    }
}
