package com.kazumaproject.kana_kanji_converter.local.system_dictionary.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_dictionary")
data class DictionaryDatabaseEntity(
    @PrimaryKey(autoGenerate = false)
    val nodeId: Int,
    val features: List<D>
)
