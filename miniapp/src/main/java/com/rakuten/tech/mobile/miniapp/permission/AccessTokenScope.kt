package com.rakuten.tech.mobile.miniapp.permission

import androidx.annotation.Keep

/**
 *  Contains the components which need to be validated when access token is granted.
 *  @property audience The service of access token.
 *  @property scopes List of areas that token can access.
 */
@Keep
data class AccessTokenScope(
    val audience: String,
    val scopes: List<String>
) {
    constructor() : this(audience = "", scopes = mutableListOf())
}
