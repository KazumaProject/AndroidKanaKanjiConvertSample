package com.kazumaproject.kana_kanji_converter.local.system_dictionary.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tango_table")
data class Tango(
    val tango: String,
    @PrimaryKey(autoGenerate = false)
    var tangoId: Int,
)
