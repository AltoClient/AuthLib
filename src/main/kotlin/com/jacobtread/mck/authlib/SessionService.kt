package com.jacobtread.mck.authlib

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.jacobtread.mck.authlib.data.ProfileTexture
import com.jacobtread.mck.authlib.exceptions.AuthException
import com.jacobtread.mck.authlib.exceptions.AuthUnavailableException
import com.jacobtread.mck.authlib.exceptions.InsecureTextureException
import com.jacobtread.mck.authlib.models.JoinServerRequest
import com.jacobtread.mck.authlib.properties.PropertyMap
import com.jacobtread.mck.utils.json.UUIDTypeAdapter
import com.jacobtread.mck.utils.network.urlEncode
import java.net.URL
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * SessionService Used for accessing session related mojang
 * API endpoints
 *
 * @constructor Create empty SessionService
 */
object SessionService {
    /**
     * HOST The root url for session related API calls
     */
    const val HOST = "https://sessionserver.mojang.com/session/minecraft"

    /**
     * ENUM_MAP_TYPE A JSON Type for an enum map for the textures
     */
    private val ENUM_MAP_TYPE = object : TypeToken<EnumMap<ProfileTexture.Type, ProfileTexture>>() {}

    /**
     * insecureProfiles A cache that stores insecure profiles for 6 hours
     */
    private val insecureProfiles = CacheBuilder
        .newBuilder()
        .expireAfterWrite(6, TimeUnit.HOURS)
        .build(object : CacheLoader<GameProfile, GameProfile>() {
            override fun load(key: GameProfile): GameProfile {
                return fillGameProfile(key, false)
            }
        })

    /**
     * publicKey The public key to validate properties of game profiles
     * the public key is loaded from yggdrasil_session_pubkey.der in the
     * root of the jar. WILL BREAK IF NOT INCLUDED
     */
    val publicKey: PublicKey = try {
        val resource = javaClass.getResourceAsStream("/yggdrasil_session_pubkey.der")
            ?: throw Error("No yggdrasil public key in classpath")
        val spec = X509EncodedKeySpec(resource.readAllBytes())
        val keyFactory = KeyFactory.getInstance("RSA")
        keyFactory.generatePublic(spec)
    } catch (ignored: Exception) {
        throw Error("Missing/invalid yggdrasil public key!")
    }

    /**
     * joinServer Lets the minecraft session server know that the provided
     * [profile] has joined the server that has the ID of [serverId]. Using
     * the access token of [accessToken]
     *
     * @param profile The game profile joining the server
     * @param accessToken The access token to authenticate the request
     * @param serverId The id of the server (Usually a hash of the serverId + publicKey + privateKey)
     */
    fun joinServer(profile: GameProfile, accessToken: String, serverId: String) {
        val request = JoinServerRequest(accessToken, profile.id, serverId)
        RootService.makeRequest(URL("$HOST/join"), request)
    }

    /**
     * hasJoinedServer Checks if the provided [profile] has joined the
     * provided [serverId] and returns the [GameProfile] if it has joined
     *
     * @param profile The game profile to check for
     * @param serverId The server ID to check for
     * @return The game profile if joined otherwise null
     */
    fun hasJoinedServer(profile: GameProfile, serverId: String): GameProfile? {
        val name = urlEncode(profile.name ?: "")
        val id = urlEncode(serverId)
        val url = URL("$HOST/hasJoined?username=$name&serverId=$id")
        try {
            val response = RootService.makeRequest(url)
            val json = RootService.GSON.fromJson(response, JsonObject::class.java)
            if (json == null || !json.has("id")) return null
            val uuid: UUID = RootService.GSON.fromJson(json["id"], UUID::class.java)
            val gameProfile = GameProfile(uuid, profile.name)
            val properties = if (json.has("properties")) RootService.GSON.fromJson(
                json["properties"],
                PropertyMap::class.java
            ) else null
            if (properties != null) {
                gameProfile.properties.putAll(properties)
            }
            return gameProfile
        } catch (e: AuthUnavailableException) {
            throw e
        } catch (e: AuthException) {
            return null
        }
    }

    /**
     * getTextures Retrieves the textures' payload from the Mojang API
     * and if [requireSecure] The returned properties will be checked
     * to ensure the signature matches the [publicKey] otherwise
     * an [InsecureTextureException] will be thrown
     *
     * @param profile The profile to get the textures for
     * @param requireSecure Whether to enforce secure textures only
     * @return The resulting enum map of texture type to texture
     */
    fun getTextures(profile: GameProfile, requireSecure: Boolean): EnumMap<ProfileTexture.Type, ProfileTexture> {
        val textureProperty = profile.properties.get("textures").firstOrNull()
            ?: return EnumMap(ProfileTexture.Type::class.java)
        if (requireSecure) {
            if (!textureProperty.hasSignature) throw InsecureTextureException("Signature is missing from textures payload")
            if (!textureProperty.isSignatureValid(publicKey)) throw InsecureTextureException("Textures payload has been tampered with (signature invalid)")
        }
        try {
            val payload = String(Base64.getDecoder().decode(textureProperty.value), Charsets.UTF_8)
            val json = RootService.GSON.fromJson(payload, JsonObject::class.java)
            if (json != null && json.has("textures")) {
                val textures = RootService.GSON.fromJson<EnumMap<ProfileTexture.Type, ProfileTexture>>(
                    json["textures"],
                    ENUM_MAP_TYPE.type
                )
                if (textures != null) {
                    return textures
                }
            }
        } catch (_: JsonParseException) {
        }
        return EnumMap(ProfileTexture.Type::class.java)
    }

    /**
     * fillProfileProperties Wrapper method of [fillGameProfile] that
     * allows the use of [insecureProfiles] if [requireSecure] is
     * set to false
     *
     * @see fillGameProfile
     * @param profile The game profile to fill
     * @param requireSecure Whether to require secure properties
     * @return The resulting game profile
     */
    fun fillProfileProperties(profile: GameProfile, requireSecure: Boolean): GameProfile {
        if (profile.id == null) return profile
        if (!requireSecure) return insecureProfiles.getUnchecked(profile)
        return fillGameProfile(profile, true)
    }

    /**
     * fillGameProfile Fills the provided game profile with game
     * profile properties from the mojang API. If [requireSecure]
     * is set to true the API will not allow unsigned
     *
     * @param profile The profile to fill
     * @param requireSecure Whether to retrieve signed properties only
     * @return The game profile that was retrieved
     */
    private fun fillGameProfile(profile: GameProfile, requireSecure: Boolean): GameProfile {
        try {
            val id = UUIDTypeAdapter.fromUUID(profile.id!!)
            val url = URL("$HOST/profile/$id?unsigned=${!requireSecure}")
            val response = RootService.makeRequest(url)
            val json = RootService.GSON.fromJson(response, JsonObject::class.java)
            if (json != null) {
                val uuid = if (json.has("id")) RootService.GSON.fromJson(json["id"], UUID::class.java) else null
                val result = GameProfile(uuid, json["name"]?.asString)
                val properties = RootService.GSON.fromJson(json["properties"], PropertyMap::class.java)
                result.properties.putAll(properties)
                profile.properties.putAll(properties)
                return result
            }
        } catch (e: AuthException) {
            return profile
        }
        return profile
    }

}