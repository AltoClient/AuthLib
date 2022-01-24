package com.jacobtread.mck.authlib.properties

import com.google.common.collect.ForwardingMultimap
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import com.google.gson.*
import com.jacobtread.mck.utils.nbt.*
import com.jacobtread.mck.utils.nbt.types.NBTCompound
import java.lang.reflect.Type

class PropertyMap : ForwardingMultimap<String, Property>(), NBTMutableSerializable {
    private val properties = LinkedHashMultimap.create<String, Property>()

    override fun delegate(): Multimap<String, Property> {
        return properties
    }

    override fun readFromNBT(nbt: NBTCompound) {
        if (nbt.hasKey("Properties", COMPOUND)) {
            val propertiesTag = nbt.getCompoundTag("Properties")
            for (name in propertiesTag.getKeySet()) {
                val properties = propertiesTag.getTagList(name, COMPOUND)
                for (propertyTag in properties.iteratorTyped<NBTCompound>()) {
                    val value = propertiesTag.getString("Value")
                    val signature = if (propertyTag.hasKey("Signature", STRING)) {
                        propertyTag.getString("Signature")
                    } else null
                    this.properties.put(name, Property(name, value, signature))
                }
            }
        }
    }

    override fun writeToNBT(nbt: NBTCompound) {
        if (properties.isEmpty) return
        nbt.setTag("Properties", nbtCompoundOf {
            val keys = properties.keys()
            keys.forEach { key ->
                setTag(key, nbtListOf {
                    properties.get(key).forEach {
                        add(nbtCompoundOf {
                            setString("Value", it.value)
                            if (it.signature != null) {
                                setString("Signature", it.signature)
                            }
                        })
                    }
                })
            }
        })
    }

    class Serializer : JsonSerializer<PropertyMap>, JsonDeserializer<PropertyMap> {
        override fun serialize(src: PropertyMap, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val root = JsonArray()
            src.values().forEach {
                val propertyObj = JsonObject()
                propertyObj.apply {
                    addProperty("name", it.name)
                    addProperty("value", it.value)
                    if (it.signature != null) {
                        addProperty("signature", it.signature)
                    }
                }
                root.add(propertyObj)
            }
            return root
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): PropertyMap {
            val result = PropertyMap()
            if (json is JsonObject) {
                json.entrySet().forEach { (key, value) ->
                    if (value is JsonArray) {
                        value.forEach { result.put(key, Property(key, it.asString)) }
                    }
                }
            } else if (json is JsonArray) {
                json.forEach {
                    if (it is JsonObject) {
                        val name = it["name"].asString
                        val value = it["value"].asString
                        val signature = if (it.has("signature")) {
                            it["signature"].asString
                        } else null
                        result.put(name, Property(name, value, signature))
                    }
                }
            }
            return result
        }

    }

}