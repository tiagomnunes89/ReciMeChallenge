package com.recime.challenge.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal class StringListConverter {

    @TypeConverter
    fun fromList(values: List<String>): String = Json.encodeToString(SERIALIZER, values)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(SERIALIZER, value)

    private companion object {
        val SERIALIZER = ListSerializer(String.serializer())
    }
}
