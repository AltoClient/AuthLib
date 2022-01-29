package com.jacobtread.alto.authlib.data

import com.jacobtread.alto.authlib.GameProfile
import com.jacobtread.alto.authlib.UserType
import com.jacobtread.alto.utils.json.UUIDTypeAdapter

data class Session(val username: String, val uuid: String, val token: String, val type: UserType) {

    fun getProfile(): GameProfile {
        return try {
            val uuid = UUIDTypeAdapter.fromString(uuid)
            GameProfile(uuid, username)
        } catch (e: IllegalArgumentException) {
            GameProfile(null, username)
        }
    }
}