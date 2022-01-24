package com.jacobtread.mck.authlib.models

import com.google.gson.annotations.SerializedName

class ValidateRequest(
    @SerializedName("clientToken") val clientToken: String,
    @SerializedName("accessToken") val accessToken: String
)