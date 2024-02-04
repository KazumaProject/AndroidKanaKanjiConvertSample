package com.kazumaproject.kana_kanji_converter.local

import androidx.room.*
import com.kazumaproject.kana_kanji_converter.local.entity.DictionaryDatabaseEntity
import com.kazumaproject.kana_kanji_converter.models.DictionaryEntry

@Dao
interface DictionaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDictionaryEntry(dictionaryDatabaseEntity: DictionaryDatabaseEntity)

    @Query("SELECT * FROM dictionaryDatabaseEntity")
    suspend fun getDictionaryEntryList(): List<DictionaryDatabaseEntity>

    @Query("SELECT * FROM dictionaryDatabaseEntity WHERE nodeId = :nodeId")
    suspend fun getDictionaryEntryListFromNodeId(nodeId: Int): List<DictionaryDatabaseEntity>

    @Query("DELETE FROM dictionaryDatabaseEntity WHERE nodeId = :nodeId")
    suspend fun deleteDictionaryEntryByNodeId(nodeId: Int)
}