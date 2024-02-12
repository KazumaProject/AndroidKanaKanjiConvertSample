package com.kazumaproject.kana_kanji_converter.local.connection_id

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kazumaproject.kana_kanji_converter.local.connection_id.entity.ConnectionID

@Database(
    entities = [ConnectionID::class],
    version = 5
)
abstract class ConnectionIDDatabase : RoomDatabase() {
    abstract val connectionIDDao: ConnectionIDDao
}