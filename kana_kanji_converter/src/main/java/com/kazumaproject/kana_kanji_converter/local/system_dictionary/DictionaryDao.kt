package com.kazumaproject.kana_kanji_converter.local.system_dictionary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.entity.Tango

@Dao
interface DictionaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTango(tango: Tango)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTangoList(tangoList: List<Tango>)

    @Query("SELECT * FROM tango_table")
    suspend fun getTangoList(): List<Tango>

    @Query("SELECT * FROM tango_table WHERE tangoId = :tangoId")
    suspend fun getTangoListFromTangoId(tangoId: String): Tango

    @Query("SELECT * FROM tango_table WHERE tango = :string")
    fun getTangoFromString(string: String): Tango


}