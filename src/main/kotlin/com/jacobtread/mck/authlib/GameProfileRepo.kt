package com.jacobtread.mck.authlib

import com.google.gson.JsonObject
import com.jacobtread.mck.authlib.exceptions.AuthException
import java.net.URL

/**
 * GameProfileRepo A Repository for fetching game profiles from
 * the mojang profiles API
 *
 * @constructor Create empty GameProfileRepo
 */
object GameProfileRepo {

    /**
     * getProfilesByName Queries the mojang profiles API for a game profile
     * with the name of [name] if none are found or an error occurs null is
     * returned instead
     *
     * @param name The name of the profile
     * @return The game profile or null
     */
    fun getProfilesByName(name: String): GameProfile? {
        if (name.isBlank()) return null
        try {
            val response = RootService.makeRequest(URL("https://api.mojang.com/profiles/minecraft/"), listOf(name))
            val json = RootService.GSON.fromJson(response, JsonObject::class.java)
            if (json != null && json.has("profiles")) {
                val profiles = RootService.GSON.fromJson(json["profiles"], Array<GameProfile>::class.java)
                if (profiles != null && profiles.isNotEmpty()) {
                    return profiles.first()
                }
            }
        } catch (_: AuthException) {
        }
        return null
    }
}