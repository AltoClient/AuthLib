package com.jacobtread.mck.authlib.data

import com.jacobtread.mck.authlib.GameProfile
import com.jacobtread.mck.authlib.UserType
import com.jacobtread.mck.utils.json.UUIDTypeAdapter

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