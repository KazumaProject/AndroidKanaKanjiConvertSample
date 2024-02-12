package com.kazumaproject.kana_kanji_converter.local.system_dictionary

import androidx.room.*
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.entity.DictionaryDatabaseEntity

@Dao
interface DictionaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDictionaryEntry(dictionaryDatabaseEntity: DictionaryDatabaseEntity)

    @Query("SELECT * FROM system_dictionary")
    suspend fun getDictionaryEntryList(): List<DictionaryDatabaseEntity>

    @Query("SELECT * FROM system_dictionary WHERE nodeId = :nodeId")
    suspend fun getDictionaryEntryListFromNodeId(nodeId: Int): List<DictionaryDatabaseEntity>

    @Query("DELETE FROM system_dictionary WHERE nodeId = :nodeId")
    suspend fun deleteDictionaryEntryByNodeId(nodeId: Int)
}