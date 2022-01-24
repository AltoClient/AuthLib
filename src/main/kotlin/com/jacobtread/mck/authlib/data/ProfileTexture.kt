package com.jacobtread.mck.authlib.data

class ProfileTexture(val url: String, val metadata: Map<String, String>) {

    fun getBaseName(): String = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'))

    fun getMetadata(name: String): String? = metadata[name]

    enum class Type { SKIN, CAPE, ELYTRA }
}