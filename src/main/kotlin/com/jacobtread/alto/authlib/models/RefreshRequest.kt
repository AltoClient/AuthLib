package com.jacobtread.alto.authlib.models

import com.google.gson.annotations.SerializedName
import com.jacobtread.alto.authlib.GameProfile

class RefreshRequest(
    @SerializedName("clientToken") val clientToken: String,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("selectedProfile") val selectedProfile: GameProfile? = null,
    @SerializedName("requestUser") val requestUser: Boolean = true
)