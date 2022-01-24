package com.jacobtread.mck.authlib.models

import com.google.gson.annotations.SerializedName
import java.util.*

class JoinServerRequest(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("selectedProfile") val selectedProfile: UUID?,
    @SerializedName("serverId") val serverId: String
)