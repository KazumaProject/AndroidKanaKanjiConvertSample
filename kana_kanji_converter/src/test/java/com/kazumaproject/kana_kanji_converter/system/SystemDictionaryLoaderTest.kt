package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kazumaproject.kana_kanji_converter.local.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.DictionaryDatabaseConverter
import com.kazumaproject.kana_kanji_converter.local.SystemDictionaryDatabase
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
            .databaseBuilder(context,SystemDictionaryDatabase::class.java,"system_dictionary")
            .addTypeConverter(DictionaryDatabaseConverter(moshi))
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
        val dictionary = dictionaryDao.getDictionaryEntryList()
        println("${dictionary.size}")
        val loudsTrie = TailLOUDSTrie()
        withContext(Dispatchers.IO) {
            loudsTrie.readExternal(ObjectInputStream(context.assets.open("system_trie/yomi.dic")))
        }
        val expected = loudsTrie.size()
        assertEquals(expected, dictionary.size)
    }

}