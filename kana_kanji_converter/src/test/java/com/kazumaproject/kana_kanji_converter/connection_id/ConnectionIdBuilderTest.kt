package com.kazumaproject.kana_kanji_converter.connection_id

import android.content.Context
import androidx.room.Room
import com.kazumaproject.kana_kanji_converter.local.connection_id.ConnectionIDDao
import com.kazumaproject.kana_kanji_converter.local.connection_id.ConnectionIDDatabase
import com.kazumaproject.kana_kanji_converter.local.connection_id.entity.ConnectionID
import com.kazumaproject.kana_kanji_converter.system.connection_id.ConnectionIdDatabaseBuilder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import java.io.BufferedReader
import java.io.InputStreamReader

@RunWith(RobolectricTestRunner::class)
@Config(assetDir = "src/test/assets")
class ConnectionIdBuilderTest {
    private lateinit var context: Context
    private lateinit var connectionIDDatabase: ConnectionIDDatabase
    private lateinit var connectionIDDao: ConnectionIDDao
    private lateinit var connectionIdDatabaseBuilder: ConnectionIdDatabaseBuilder

    @Before
    fun setConnectionIdDatabase(){
        context = RuntimeEnvironment.getApplication()
        connectionIDDatabase = Room
            .databaseBuilder(context,ConnectionIDDatabase::class.java,"connection_id_table")
            .build()
        connectionIDDao = connectionIDDatabase.connectionIDDao

        connectionIdDatabaseBuilder = ConnectionIdDatabaseBuilder(context)
    }

    @After
    fun closeConnectionIdDatabase(){
        connectionIDDatabase.close()
    }

    @Test
    fun `Test insert connection id from connection_single_column`() = runBlocking {
        val connectionIdFromAsset = BufferedReader(InputStreamReader(context.assets.open("connection_id/connection_single_column.txt")))
        val connectionIdList = connectionIdFromAsset.readLines()
        for (i in connectionIdList.indices){
            val cost = connectionIdList[i].toInt()
            val connectionID = ConnectionID(
                cID = i,
                cost = cost
            )
            println("insert cId:${connectionID.cID} cost: ${connectionID.cost}")
            connectionIDDao.insertConnectionID(connectionID)
        }
        val expected = connectionIdList.size
        assertEquals(expected, connectionIDDao.getAllConnectionId().size)
    }

    @Test
    fun `Test connection id list size from database`() = runBlocking {
        println("${connectionIdDatabaseBuilder.getConnectionIdList().size}")
    }
}