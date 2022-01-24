package com.jacobtread.mck.authlib.models

import com.google.gson.annotations.SerializedName
import com.jacobtread.mck.authlib.GameProfile

class RefreshRequest(
    @SerializedName("clientToken") val clientToken: String,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("selectedProfile") val selectedProfile: GameProfile? = null,
    @SerializedName("requestUser") val requestUser: Boolean = true
)