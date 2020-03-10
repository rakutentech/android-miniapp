package com.rakuten.tech.mobile.miniapp

import android.webkit.URLUtil

/**
 * Config for the Mini App SDK.
 * Contains settings which are used when sending requests to the Mini App API.
 * @property baseUrl Base API url.
 * @property rasAppId Rakuten App Studio project id.
 * @property subscriptionKey Subscription key which can be retrieved as Primary or Secondary key.
 * @property hostAppVersionId Specific application id on Rakuten App Studio project.
 */
data class MiniAppSdkConfig(
    var baseUrl: String,
    var rasAppId: String,
    var subscriptionKey: String,
    var hostAppVersionId: String
) {
    internal val key = "$baseUrl-$rasAppId-$subscriptionKey-$hostAppVersionId"

    init {
        when {
            !URLUtil.isHttpsUrl(baseUrl) ->
                throw MiniAppSdkException("MiniAppSdkConfig with invalid baseUrl: $baseUrl")
            rasAppId.isBlank() ->
                throw MiniAppSdkException("MiniAppSdkConfig with invalid rasAppId: $rasAppId")
            subscriptionKey.isBlank() ->
                throw MiniAppSdkException("MiniAppSdkConfig with invalid subscriptionKey: $subscriptionKey")
            hostAppVersionId.isBlank() ->
                throw MiniAppSdkException("MiniAppSdkConfig with invalid hostAppVersionId: $hostAppVersionId")
        }
    }
}
