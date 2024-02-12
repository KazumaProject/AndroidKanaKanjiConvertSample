package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import androidx.room.Room
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.DictionaryDatabaseConverter
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.SystemDictionaryDatabase
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.entity.D
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.entity.DictionaryDatabaseEntity
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
class SystemDictionaryDataBaseBuilderTest {
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
            .inMemoryDatabaseBuilder(
            context,
            SystemDictionaryDatabase::class.java
        )
            .addTypeConverter(DictionaryDatabaseConverter(moshi))
            .allowMainThreadQueries()
            .build()
        dictionaryDao = systemDicDatabase.dictionaryDao
    }

    @After
    fun closeSystemDictionaryDatabase(){
        systemDicDatabase.close()
    }

    @Test
    fun `Test insert dummy system dictionary entry`() = runBlocking {
        val test = DictionaryDatabaseEntity(1234, listOf())
        dictionaryDao.insertDictionaryEntry(test)
        println("${dictionaryDao.getDictionaryEntryList()}")
    }

    @Test
    fun `Test insert dictionary00`() = runBlocking {
        val dictionary00 = systemDictionaryBuilder.groupAllDictionaries(
            listOf(
                "dictionaries/dictionary00.txt",
            ),
            "single_kanji/single_kanji.tsv"
        )
        val loudsTrie = TailLOUDSTrie()
        withContext(Dispatchers.IO) {
            loudsTrie.readExternal(ObjectInputStream(context.assets.open("system_trie/yomi.dic")))
        }
        dictionary00.forEach { entry ->
            val nodeId = loudsTrie.getNodeId(entry.key)
            val dictionaryEntry = DictionaryDatabaseEntity(nodeId,entry.value.map {
                D(
                    l = it.leftID,
                    r = it.rightID,
                    c = it.wordCost,
                    t = it.afterConversion
                )
            })
            println("insert: $nodeId ${entry.value}")
            dictionaryDao.insertDictionaryEntry(dictionaryEntry)
        }
        println("${dictionaryDao.getDictionaryEntryList().size} ${dictionary00.size}")
        val expected = dictionary00.size
        assertEquals(expected, dictionaryDao.getDictionaryEntryList().size)
    }

    @Test
    fun `Test insert dictionary00 and 01`() = runBlocking {
        val dictionaries = systemDictionaryBuilder.groupAllDictionaries(
            listOf(
                "dictionaries/dictionary00.txt",
                "dictionaries/dictionary01.txt",
            ),
            "single_kanji/single_kanji.tsv"
        )
        val loudsTrie = TailLOUDSTrie()
        withContext(Dispatchers.IO) {
            loudsTrie.readExternal(ObjectInputStream(context.assets.open("system_trie/yomi.dic")))
        }
        dictionaries.forEach { entry ->
            val nodeId = loudsTrie.getNodeId(entry.key)
            val dictionaryEntry = DictionaryDatabaseEntity(nodeId,entry.value.map {
                D(
                    l = it.leftID,
                    r = it.rightID,
                    c = it.wordCost,
                    t = it.afterConversion
                )
            })
            println("insert: $nodeId ${entry.value}")
            dictionaryDao.insertDictionaryEntry(dictionaryEntry)
        }
        println("${dictionaryDao.getDictionaryEntryList().size} ${dictionaries.size}")
        val expected = dictionaries.size
        assertEquals(expected, dictionaryDao.getDictionaryEntryList().size)
    }

    @Test
    fun `Test insert dictionary00,01 and 02`() = runBlocking {
        val dictionaries = systemDictionaryBuilder.groupAllDictionaries(
            listOf(
                "dictionaries/dictionary00.txt",
                "dictionaries/dictionary01.txt",
                "dictionaries/dictionary02.txt",
            ),
            "single_kanji/single_kanji.tsv"
        )
        val loudsTrie = TailLOUDSTrie()
        withContext(Dispatchers.IO) {
            loudsTrie.readExternal(ObjectInputStream(context.assets.open("system_trie/yomi.dic")))
        }
        dictionaries.forEach { entry ->
            val nodeId = loudsTrie.getNodeId(entry.key)
            val dictionaryEntry = DictionaryDatabaseEntity(nodeId,entry.value.map {
                D(
                    l = it.leftID,
                    r = it.rightID,
                    c = it.wordCost,
                    t = it.afterConversion
                )
            })
            println("insert: $nodeId ${entry.value}")
            dictionaryDao.insertDictionaryEntry(dictionaryEntry)
        }
        println("${dictionaryDao.getDictionaryEntryList().size} ${dictionaries.size}")
        val expected = dictionaries.size
        assertEquals(expected, dictionaryDao.getDictionaryEntryList().size)
    }

}