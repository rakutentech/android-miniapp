package com.rakuten.tech.mobile.testapp

import android.app.Activity
import android.content.Intent

/**
 * When the activity is top launcher then only one instance in a task.
 */
inline fun <reified T : Any> Activity.launchActivity(noinline init: Intent.() -> Unit = {}) {
    startActivity(
        Intent(this, T::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
    finish()
}
