package com.kazumaproject.kana_kanji_converter.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DictionaryDatabaseEntity(
    @PrimaryKey(autoGenerate = false)
    val nodeId: Int,
    val features: List<D>
)
