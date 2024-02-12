package com.kazumaproject.kana_kanji_converter.local.system_dictionary

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kazumaproject.kana_kanji_converter.local.connection_id.ConnectionIDDao
import com.kazumaproject.kana_kanji_converter.local.connection_id.entity.ConnectionID
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.entity.DictionaryDatabaseEntity

@Database(
    entities = [DictionaryDatabaseEntity::class,ConnectionID::class],
    version = 5
)
@TypeConverters(
    DictionaryDatabaseConverter::class
)
abstract class SystemDictionaryDatabase : RoomDatabase(){
    abstract val dictionaryDao: DictionaryDao
    abstract val connectionIDDao: ConnectionIDDao
}