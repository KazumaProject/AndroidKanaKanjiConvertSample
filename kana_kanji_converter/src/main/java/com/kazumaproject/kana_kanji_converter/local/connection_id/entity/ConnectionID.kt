package com.kazumaproject.kana_kanji_converter.local.connection_id.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_id_table")
data class ConnectionID(
    @PrimaryKey(autoGenerate = false)
    val cID: Int,
    val cost: Int
)
