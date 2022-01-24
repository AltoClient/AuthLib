package com.jacobtread.mck.authlib

import com.google.gson.JsonObject
import com.jacobtread.mck.authlib.exceptions.AuthException
import com.jacobtread.mck.authlib.exceptions.InvalidCredentialsException
import com.jacobtread.mck.authlib.models.AuthRequest
import com.jacobtread.mck.authlib.models.RefreshRequest
import com.jacobtread.mck.authlib.models.ValidateRequest
import com.jacobtread.mck.utils.json.expectString
import java.net.URL

object AuthService {

    val gson = RootService.GSON
    val clientToken = RootService.clientToken

    var profiles = emptyArray<GameProfile>()
    var accessToken: String? = null

    var userType: UserType = UserType.MOJANG
    var selectedProfile: GameProfile? = null

    /**
     * login Logs into a mojang account using the provided [username]
     * and [password]
     *
     * @throws AuthException Thrown if authentication failed
     * @param username
     * @param password
     */
    @Throws(AuthException::class)
    fun login(username: String, password: String) {
        if (username.isBlank()) throw InvalidCredentialsException("Username cannot be blank")
        if (password.isBlank()) throw InvalidCredentialsException("Password cannot be blank")
        val request = AuthRequest(username, password, clientToken)
        val response: JsonObject = RootService.makeRequest(URL("https://authserver.mojang.com/authenticate"), request)
        loginWithResponse(response)
    }

    /**
     * loginWithResponse Takes the values provided from the JSON response
     * and sets the appropriate fields to the values
     *
     * @param response The response from the server
     */
    private fun loginWithResponse(response: JsonObject) {
        val availableProfiles: Array<GameProfile> =
            gson.fromJson(response["availableProfiles"], Array<GameProfile>::class.java)
        val selectedProfile = if (response.has("selectedProfile")) gson.fromJson(
            response["selectedProfile"],
            GameProfile::class.java
        ) else null
        userType = if (selectedProfile != null) {
            if (selectedProfile.isLegacy) UserType.LEGACY else UserType.MOJANG
        } else {
            if (availableProfiles[0].isLegacy) UserType.LEGACY else UserType.MOJANG
        }
        profiles = availableProfiles
        this.selectedProfile = selectedProfile
        accessToken = response.expectString("accessToken")
    }

    /**
     * login Logs into a mojang account using the provided [token]
     * will refresh the token if it's not valid
     *
     * @throws AuthException Thrown if authentication failed
     * @param token The access token to authenticate with
     */
    @Throws(AuthException::class)
    fun login(token: String) {
        if (token.isBlank()) throw InvalidCredentialsException("Token cannot be blank")
        val validityRequest = ValidateRequest(clientToken, token)
        try {
            RootService.makeRequest(URL("https://authserver.mojang.com/validate"), validityRequest)
        } catch (e: AuthException) {
            // Token is invalid refresh it
            val refreshRequest = RefreshRequest(clientToken, token)
            val response: JsonObject =
                RootService.makeRequest(URL("https://authserver.mojang.com/refresh"), refreshRequest)
            loginWithResponse(response)
        }
    }

}