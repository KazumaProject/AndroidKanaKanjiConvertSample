package com.kazumaproject.kana_kanji_converter.local.system_dictionary

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.entity.D
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@ProvidedTypeConverter
class DictionaryDatabaseConverter(
    private val moshi: Moshi
) {
    @TypeConverter
    fun stringToDictionary(json: String): List<D> {
        val listType = Types.newParameterizedType(List::class.java, D::class.java)
        return moshi.adapter<List<D>>(listType).fromJson(json) ?: emptyList()
    }

    @TypeConverter
    fun dictionaryToString(attendees: List<D>): String {
        val listType = Types.newParameterizedType(List::class.java, D::class.java)
        return moshi.adapter<List<D>>(listType).toJson(attendees)
    }
}