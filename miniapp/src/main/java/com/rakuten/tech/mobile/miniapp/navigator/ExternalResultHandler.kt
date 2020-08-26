package com.rakuten.tech.mobile.miniapp.navigator

import kotlin.properties.Delegates.observable

/** The management of callback operations between external factors and mini app view. **/
class ExternalResultHandler {

    /** Result map key.
     *  @property shouldClose determine to close external url loader. i.e: There is case that the specific
     * url redirect only works on sdk mini app view so should close the external loader and
     * emit that url to mini app view by [emitResult]
     */
    companion object {
        val URL = "external_url"

        var shouldClose: (url: String) -> Boolean = { false }
        internal set(value) { field = value }
    }

    private var result: Map<String, String> by observable(HashMap()) { _, _, newValue ->
        onResultChanged?.invoke(newValue)
    }
    internal var onResultChanged: ((Map<String, String>) -> Unit)? = null

    // Notify the result to mini app view.
    fun emitResult(mapResult: Map<String, String>) {
        result = mapResult
    }
}
