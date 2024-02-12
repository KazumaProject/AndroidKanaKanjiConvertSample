package com.kazumaproject.kana_kanji_converter.system.connection_id

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kazumaproject.kana_kanji_converter.local.connection_id.ConnectionIDDao
import com.kazumaproject.kana_kanji_converter.local.connection_id.ConnectionIDDatabase
import com.kazumaproject.kana_kanji_converter.local.connection_id.entity.ConnectionID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class ConnectionIdDatabaseBuilder(val context: Context) {

    private val connectionIDDatabase: ConnectionIDDatabase = Room
        .databaseBuilder(context,ConnectionIDDatabase::class.java,"connection_id_table")
        .build()
    private val connectionIDDao: ConnectionIDDao = connectionIDDatabase.connectionIDDao

    private val connectionIdDatabasePrepopulate: ConnectionIDDatabase = Room
        .databaseBuilder(context,ConnectionIDDatabase::class.java,"connection_id_table")
        .createFromAsset("connection_id_database/connection_id_table")
        .addMigrations(object : Migration(5,6){
            override fun migrate(db: SupportSQLiteDatabase) {
            }

        })
        .build()
    private val connectionIdDaoPrepopulate = connectionIdDatabasePrepopulate.connectionIDDao

    suspend fun insertConnectionIdFromFile(fileName: String) = CoroutineScope(Dispatchers.IO).launch{
        val reader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
        val lines = reader.readLines()
        for (i in lines.indices){
            val connectionCost = lines[i].toInt()
            val connectionID = ConnectionID(
                cID = i,
                cost = connectionCost
            )
            Log.d("insert connection id","id: ${connectionID.cID} cost: ${connectionID.cost}")
            connectionIDDao.insertConnectionID(connectionID)
        }
    }

    suspend fun getConnectionIdList(): List<ConnectionID> = connectionIdDaoPrepopulate.getAllConnectionId()

}