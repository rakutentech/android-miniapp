package com.rakuten.tech.mobile.miniapp.navigator

import kotlin.properties.Delegates.observable

/**
 * The url transmitter from external factors to mini app view.
 **/
class ExternalUrlHandler {

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
}
