package com.jacobtread.mck.authlib

import com.google.gson.*
import com.jacobtread.mck.authlib.properties.PropertyMap
import com.jacobtread.mck.utils.json.UUIDTypeAdapter
import com.jacobtread.mck.utils.json.expectStringOrNull
import com.jacobtread.mck.utils.nbt.*
import com.jacobtread.mck.utils.nbt.types.NBTCompound
import java.lang.reflect.Type
import java.util.*

data class GameProfile(
    val id: UUID?,
    val name: String?
) {

    val properties = PropertyMap()
    val isComplete: Boolean get() = id != null && !name.isNullOrBlank()
    var isLegacy = false

    init {
        check(id != null || !name.isNullOrBlank())
        { "Game profile requires ID/Name both cannot be empty" }
    }

    object Serializer : NBTSerializer<GameProfile>, NBTDeserializer<GameProfile?> {
        override fun fromNBT(nbt: NBTBase): GameProfile? {
            if (nbt !is NBTCompound) throw NBTException("Expected compound tag at root for GameProfile")
            val name = nbt.getStringOrNull("Name")
            val id = nbt.getStringOrNull("Id")
            if (name.isNullOrBlank() && id.isNullOrBlank()) return null
            val uuid = if (id == null) null else UUIDTypeAdapter.fromStringSafe(id)
            val profile = GameProfile(uuid, name)
            profile.properties.readFromNBT(nbt)
            return profile
        }

        override fun toNBT(value: GameProfile): NBTCompound {
            return nbtCompoundOf {
                if (!value.name.isNullOrBlank()) setString("Name", value.name)
                if (value.id != null) setString("Id", value.id.toString())
                value.properties.writeToNBT(this)
            }
        }
    }

    class SerializerJson : JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): GameProfile {
            json as JsonObject
            val id = if (json.has("id")) context.deserialize<UUID>(json["id"], UUID::class.java) else null
            val name = json.expectStringOrNull("name")
            return GameProfile(id, name)
        }

        override fun serialize(src: GameProfile, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val root = JsonObject()
            if (src.id != null) root.add("id", context.serialize(src.id))
            if (src.name != null) root.addProperty("name", src.name)
            return root
        }
    }
}