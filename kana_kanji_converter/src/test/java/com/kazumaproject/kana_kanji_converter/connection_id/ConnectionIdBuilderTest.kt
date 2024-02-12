package com.kazumaproject.kana_kanji_converter.connection_id

import android.content.Context
import androidx.room.Room
import com.kazumaproject.kana_kanji_converter.local.connection_id.ConnectionIDDao
import com.kazumaproject.kana_kanji_converter.local.connection_id.entity.ConnectionID
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.DictionaryDatabaseConverter
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.SystemDictionaryDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.BufferedReader
import java.io.InputStreamReader

@RunWith(RobolectricTestRunner::class)
@Config(assetDir = "src/test/assets")
class ConnectionIdBuilderTest {
    private lateinit var context: Context
    private lateinit var systemDictionaryDatabase: SystemDictionaryDatabase
    private lateinit var connectionIDDao: ConnectionIDDao

    @Before
    fun setConnectionIdDatabase(){
        context = RuntimeEnvironment.getApplication()
        val moshi = Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        systemDictionaryDatabase = Room
            .inMemoryDatabaseBuilder(
                context,
                SystemDictionaryDatabase::class.java
            )
            .addTypeConverter(DictionaryDatabaseConverter(moshi))
            .allowMainThreadQueries()
            .build()
        connectionIDDao = systemDictionaryDatabase.connectionIDDao
    }

    @After
    fun closeConnectionIdDatabase(){
        systemDictionaryDatabase.close()
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
        println("${systemDictionaryDatabase.connectionIDDao.getAllConnectionId().size}")
    }
}