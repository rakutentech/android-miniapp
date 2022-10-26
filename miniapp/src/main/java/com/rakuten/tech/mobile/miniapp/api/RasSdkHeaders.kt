package com.rakuten.tech.mobile.miniapp.api

/**
 * Standard headers that should be sent with all requests to RAS.
 */
internal class RasSdkHeaders(
    private val appId: Pair<String, String>,
    private val subscriptionKey: Pair<String, String>,
    private val sdkName: Pair<String, String>,
    private val sdkVersion: Pair<String, String>,
    private val deviceModel: Pair<String, String>,
    private val deviceOs: Pair<String, String>
) {

    /**
     * Returns the RAS headers as an array of [Pair]'s.
     *
     * @return array of [Pair] objects with [Pair.first] holding the header name
     * and [Pair.second] holding the header value.
     */
    fun asArray() = arrayOf(
        subscriptionKey,
        appId,
        sdkName,
        sdkVersion,
        deviceModel,
        deviceOs
    )
}
