package com.rakuten.tech.mobile.miniapp.navigator

import android.content.Intent
import kotlin.properties.Delegates.observable

/**
 * The url transmitter from external factors to mini app view.
 **/
class ExternalResultHandler {

    private var result: String by observable(String()) { _, _, newValue ->
        onResultChanged?.invoke(newValue)
    }
    internal var onResultChanged: ((String) -> Unit)? = null

    /**
     * Notify the result to mini app view.
     * @param url Return the current loading url to mini app view.
     **/
    fun emitResult(url: String) {
        result = url
    }

    /**
     * Notify the result to mini app view. Use this when go with auto close Activity approach.
     * @see [MiniAppExternalUrlLoader].
     * @param intent The result intent from closing Activity.
     **/
    fun emitResult(intent: Intent) {
        if (intent.hasExtra(MiniAppExternalUrlLoader.returnUrlTag))
            emitResult(intent.getStringExtra(MiniAppExternalUrlLoader.returnUrlTag)!!)
    }
}
