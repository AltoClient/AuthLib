package com.jacobtread.mck.authlib

/**
 * UserType Represents the type of minecraft account
 * either legacy or mojang accounts
 *
 * @constructor Create empty UserType
 */
enum class UserType {
    LEGACY, MOJANG;

    companion object {
        /**
         * byName If the value is legacy [LEGACY] will be returned
         * for any other value [MOJANG] will be returned
         *
         * @param name The name of the user type
         * @return The user type
         */
        fun byName(name: String): UserType = if (name == "legacy") LEGACY else MOJANG
    }
}