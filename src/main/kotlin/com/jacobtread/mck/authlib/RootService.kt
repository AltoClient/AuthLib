package com.jacobtread.mck.authlib

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.jacobtread.mck.authlib.exceptions.AuthException
import com.jacobtread.mck.authlib.exceptions.InsufficientPrivilegesException
import com.jacobtread.mck.authlib.exceptions.InvalidCredentialsException
import com.jacobtread.mck.authlib.exceptions.UserMigratedException
import com.jacobtread.mck.authlib.properties.PropertyMap
import com.jacobtread.mck.utils.json.UUIDTypeAdapter
import com.jacobtread.mck.utils.network.Https
import java.io.IOException
import java.net.URL
import java.util.*

/**
 * RootService Acts as a central store for [SessionService], [GameProfileRepo],
 * and [AuthService] to allow them to have a shared [clientToken] and [GSON]
 * instance
 *
 * @constructor Create empty RootService
 */
object RootService {

    /**
     * clientToken A randomly generated token to identify the client
     * used throughout different API endpoints
     */
    val clientToken = UUID.randomUUID().toString()

    /**
     * GSON A Gson instance used for serializing different API responses
     * supports serializing [GameProfile]'s [PropertyMap]'s and [UUID]'s
     */
    val GSON: Gson = GsonBuilder()
        .registerTypeAdapter(GameProfile::class.java, GameProfile.SerializerJson())
        .registerTypeAdapter(PropertyMap::class.java, PropertyMap.Serializer())
        .registerTypeAdapter(UUID::class.java, UUIDTypeAdapter())
        .create()

    /**
     * makeRequest Makes a request to the provided URL (expects a mojang api endpoint)
     * and parses the JSON response determining if any errors occurred. If none occurred
     * then the [JsonObject] will be returned otherwise an instance of [AuthException]
     * will be thrown
     *
     * @param url The url to make the request to
     * @param body The POST body of the request (if null the request will be a GET request)
     * @param authentication An optional Authentication header
     * @return
     */
    @Throws(IOException::class)
    fun makeRequest(url: URL, body: Any? = null, authentication: String? = null): JsonObject {
        try {
            val response = if (body != null) {
                Https.post(url, GSON.toJson(body), "application/json")
            } else Https.get(url, authentication)
            val json = GSON.fromJson(response, JsonObject::class.java)
            val error = json["error"]?.asString
            if (!error.isNullOrBlank()) {
                val cause = json["cause"].asString
                val errorMessage = json["errorMessage"].asString
                if ("UserMigratedException" == cause) throw UserMigratedException(errorMessage)
                else if ("ForbiddenOperationException" == error) throw InvalidCredentialsException(errorMessage)
                else if ("InsufficientPrivilegesException" == error) throw InsufficientPrivilegesException(errorMessage)
                else throw AuthException(errorMessage)
            }
            return json
        } catch (e: IOException) {
            throw AuthException("Cannot contact authentication server", e)
        } catch (e: IllegalStateException) {
            throw AuthException("Cannot contact authentication server", e)
        } catch (e: JsonParseException) {
            throw AuthException("Cannot contact authentication server", e)
        }
    }
}