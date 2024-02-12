package com.kazumaproject.kana_kanji_converter.local.connection_id

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kazumaproject.kana_kanji_converter.local.connection_id.entity.ConnectionID

@Dao
interface ConnectionIDDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnectionID(connectionID: ConnectionID)

    @Query("SELECT * FROM connection_id_table WHERE cID = :cID")
    suspend fun getCostFromConnectionID(cID: Int): ConnectionID

    @Query("SELECT * FROM connection_id_table")
    suspend fun getAllConnectionId(): List<ConnectionID>
}