package com.jacobtread.alto.authlib.models

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

class AuthRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("clientToken") val clientToken: String,
    @SerializedName("agent") val agent: JsonObject = JsonObject().apply {
        addProperty("name", "Minecraft")
        addProperty("version", 1)
    }
)