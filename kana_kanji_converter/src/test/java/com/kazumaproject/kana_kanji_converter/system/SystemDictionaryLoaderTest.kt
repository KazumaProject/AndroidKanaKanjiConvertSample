package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kazumaproject.kana_kanji_converter.local.connection_id.entity.ConnectionID
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.DictionaryDatabaseConverter
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.SystemDictionaryDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.trie4j.louds.TailLOUDSTrie
import java.io.ObjectInputStream
import org.junit.Assert.*
import java.io.BufferedReader
import java.io.InputStreamReader

@RunWith(RobolectricTestRunner::class)
@Config(assetDir = "src/test/assets")
class SystemDictionaryLoaderTest {
    private lateinit var context: Context
    private lateinit var systemDicDatabase: SystemDictionaryDatabase
    private lateinit var dictionaryDao: DictionaryDao
    private lateinit var systemDictionaryBuilder: SystemDictionaryBuilder

    @Before
    fun setUpSystemDictionaryDatabase(){
        context = RuntimeEnvironment.getApplication()
        systemDictionaryBuilder = SystemDictionaryBuilder(context)
        val moshi = Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        systemDicDatabase = Room
            .databaseBuilder(context, SystemDictionaryDatabase::class.java,"system_dictionary")
            .addTypeConverter(DictionaryDatabaseConverter(moshi))
            .createFromAsset("system_dictionary_database/system_dictionary")
            .addMigrations(object : Migration(5,6){
                override fun migrate(db: SupportSQLiteDatabase) {
                }
            })
            .build()
        dictionaryDao = systemDicDatabase.dictionaryDao
    }

    @After
    fun closeSystemDictionaryDatabase(){
        systemDicDatabase.close()
    }

    @Test
    fun `Test load yomi trie`() = runBlocking {
        val loudsTrie = TailLOUDSTrie()
        withContext(Dispatchers.IO) {
            loudsTrie.readExternal(ObjectInputStream(context.assets.open("system_trie/yomi.dic")))
        }
        println("${loudsTrie.size()}")
        val expected = 1025057
        assertEquals(expected, loudsTrie.size())
    }

    @Test
    fun `Test load system dictionary database`() = runBlocking {
        val dictionary = systemDictionaryBuilder.getAllDictionaryList()
        println("${dictionary.size}")
        val expected = 1025057
        assertEquals(expected, dictionary.size)
    }

    @Test
    fun `Test connection id database`() = runBlocking {
        val connectionIds = systemDictionaryBuilder.getAllConnectionIds()
        println("${connectionIds.size}")
        val expected = 7086244
        assertEquals(expected, connectionIds.size)
    }

    @Test
    fun `Test connection id database with text data`() = runBlocking {
        val connectionIdsFromText = systemDictionaryBuilder.getConnectionIdsFromText()
        println("${connectionIdsFromText.size}")
        println("${connectionIdsFromText[10]} ${connectionIdsFromText[100]} ${connectionIdsFromText[1000]}")
    }

    @Test
    fun `test loading connectionId_def`() = runBlocking {
        val connectionIdsFromConnectionIdDef = systemDictionaryBuilder.getConnectionIdsInShortArray()
        println(connectionIdsFromConnectionIdDef?.size)
    }

}