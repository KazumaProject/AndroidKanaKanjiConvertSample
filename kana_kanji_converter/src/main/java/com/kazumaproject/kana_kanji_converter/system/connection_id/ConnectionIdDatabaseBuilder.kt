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
        Log.d("started to insert connection id list","")
        val startTime = System.currentTimeMillis()
        val reader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
        val lines = reader.readLines().mapIndexed { index, s ->
            ConnectionID(
                cID = index,
                cost = s.toInt()
            )
        }
        connectionIDDao.insertConnectionIdList(lines)
        val endTime = System.currentTimeMillis()
        val elapsedTime = (endTime - startTime) / 1000
        Log.d("Execution time to build connection id database","$elapsedTime seconds")
        Log.d("finished to insert connection id list","")
    }

    suspend fun getConnectionIdList(): List<ConnectionID> = connectionIdDaoPrepopulate.getAllConnectionId()

}