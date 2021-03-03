package com.rakuten.tech.mobile.miniapp.permission

import androidx.annotation.Keep

/**
 *  Contains the components which need to be validated when access token is granted.
 *  @property audience The service of access token.
 *  @property scopes List of areas that token can access.
 */
@Keep
data class AccessTokenPermission(val audience: String, val scopes: List<String>) {

    fun contains(accessTokenPermission: AccessTokenPermission): Boolean =
        this.audience == accessTokenPermission.audience && this.scopes.containsAll(accessTokenPermission.scopes)
}
