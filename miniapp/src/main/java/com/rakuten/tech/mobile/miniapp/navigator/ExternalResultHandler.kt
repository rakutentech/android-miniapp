package com.rakuten.tech.mobile.miniapp.navigator

import kotlin.properties.Delegates.observable

/**
 * The management of callback operations between external factors and mini app view.
 * @property miniAppUrlSchemes contains the list of internal schemes which can be only loaded via mini app view.
 * Using this to determine url which starts with mini app scheme should be transmitted back to mini app view.
 * i.e External loader redirect to url which belongs to [miniAppUrlSchemes],
 * close the external loader and emit that url to mini app view by [emitResult].
 **/
class ExternalResultHandler {

    // Result map key.
    companion object {
        val URL = "external_url"
    }

    internal val miniAppUrlSchemes = mutableListOf<String>()

    private var result: Map<String, String> by observable(HashMap()) { _, _, newValue ->
        onResultChanged?.invoke(newValue)
    }
    internal var onResultChanged: ((Map<String, String>) -> Unit)? = null

    // Get the value of miniAppUrlSchemes, not reference.
    fun getMiniAppUrlSchemes(): Array<String> = miniAppUrlSchemes.toTypedArray()

    // Notify the result to mini app view.
    fun emitResult(mapResult: Map<String, String>) {
        result = mapResult
    }
}
