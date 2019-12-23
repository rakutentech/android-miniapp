package com.rakuten.tech.mobile.miniapp.api

internal class ListRequest

internal class ListResponse

@Suppress("UndocumentedPublicClass")
data class ListEntity(
    val id: String,
    val version: String,
    val name: String,
    val description: String,
    val icon: String,
    val ref: String
)
