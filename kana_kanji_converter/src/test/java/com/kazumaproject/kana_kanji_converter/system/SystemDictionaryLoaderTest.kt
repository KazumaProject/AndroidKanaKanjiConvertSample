package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.DictionaryDatabaseConverter
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.SystemDictionaryDatabase
import com.kazumaproject.kana_kanji_converter.model.TokenEntry
import com.kazumaproject.trie4j.doublearray.DoubleArray
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import com.kazumaproject.trie4j.doublearray.TailDoubleArray
import com.kazumaproject.trie4j.louds.MapTailLOUDSTrie
import com.kazumaproject.trie4j.louds.TailLOUDSTrie
import com.kazumaproject.trie4j.louds.TailLOUDSTrie.LOUDSNode
import com.kazumaproject.trie4j.patricia.MapTailPatriciaTrie
import com.kazumaproject.trie4j.patricia.TailPatriciaTrie
import java.io.ObjectInputStream
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(assetDir = "src/test/assets")
class SystemDictionaryLoaderTest {
    private lateinit var context: Context
    private lateinit var systemDictionaryBuilder: SystemDictionaryBuilder

    @Before
    fun setUpSystemDictionaryDatabase(){
        context = RuntimeEnvironment.getApplication()
        systemDictionaryBuilder = SystemDictionaryBuilder(context)
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
    fun `Test load token array`() = runBlocking {
        val tokenArray: ArrayList<TokenEntry>
        val objectInputStream = ObjectInputStream(context.assets.open("system_trie/token.def"))
        tokenArray = objectInputStream.readObject() as ArrayList<TokenEntry>
        println("${tokenArray.size}")
        println("${tokenArray[500]}")
    }

    @Test
    fun `Test query text`() = runBlocking {
        val yomiTrie = TailLOUDSTrie()
        withContext(Dispatchers.IO) {
            yomiTrie.readExternal(ObjectInputStream(context.assets.open("system_trie/yomi.dic")))
        }
        val tangoTrie = TailLOUDSTrie()
        withContext(Dispatchers.IO) {
            tangoTrie.readExternal(ObjectInputStream(context.assets.open("system_trie/tango.dic")))
        }
        val tokenArray: ArrayList<List<TokenEntry>>
        val objectInputStream = ObjectInputStream(context.assets.open("system_trie/token.def"))
        tokenArray = objectInputStream.readObject() as ArrayList<List<TokenEntry>>
        println("${tokenArray.size}")
        val index = yomiTrie.getNodeId("あいあんと")
        val tangoIds = tokenArray[index]
        tangoIds.forEach {

        }
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
        val connectionIds = systemDictionaryBuilder.readConnectionIdsFromBinaryFile()
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
    fun `Test trie`() = runBlocking {
        val atomicInteger = AtomicInteger()

        val dictionaryTexts = systemDictionaryBuilder.groupAllDictionaries(
            listOf(
                "dictionaries/dictionary00.txt",
            ),
            "single_kanji/single_kanji.tsv"
        )
        val yomiTrie = TailPatriciaTrie()
        val tangoTrie = MapTailPatriciaTrie<String>()

        dictionaryTexts.forEach { entry ->
            yomiTrie.insert(entry.key)
            println("insert yomi trie: ${entry.key}")

            entry.value.forEach {
                val id = atomicInteger.incrementAndGet().toString()
                tangoTrie.insert(id,it.afterConversion)
                println("insert tango trie: $id ${it.afterConversion}")
            }

        }

        val yomiLoudsTrie = TailLOUDSTrie(yomiTrie)
        val tangoLoudsTrie = MapTailLOUDSTrie<String>(tangoTrie)

        //40122
        val queryText = "アイアンと"
        val nodeIdInTangoTrie = tangoLoudsTrie.get("1")

        println("${yomiLoudsTrie.size()}")
        println("${tangoLoudsTrie.size()}")

        println("${yomiLoudsTrie.contains("あいあんと")}")

        println(nodeIdInTangoTrie)
    }

    @Test
    fun `Test trie2`() = runBlocking {
        val atomicInteger = AtomicInteger()

        val dictionaryTexts = systemDictionaryBuilder.groupAllDictionaries(
            listOf(
                "dictionaries/dictionary00.txt",
            ),
            "single_kanji/single_kanji.tsv"
        )
        val yomiTrie = TailPatriciaTrie()
        val tangoTrie = TailPatriciaTrie()

        dictionaryTexts.forEach { entry ->
            yomiTrie.insert(entry.key)
            println("insert yomi trie: ${entry.key}")

            entry.value.forEach {
                val id = atomicInteger.incrementAndGet().toString()
                tangoTrie.insert(it.afterConversion)
                println("insert tango trie: $id ${it.afterConversion}")
            }

        }

        val yomiLoudsTrie = TailLOUDSTrie(yomiTrie)
        val tangoLoudsTrie = TailLOUDSTrie(tangoTrie)

        //40122
        val queryText = "アイアンと"
        val nodeIdInTangoTrie = tangoLoudsTrie.getNodeId(queryText)

        val tangoFromNodeId = tangoLoudsTrie.LOUDSNode(nodeIdInTangoTrie).letters.joinToString()

        println("${yomiLoudsTrie.size()}")
        println("${tangoLoudsTrie.size()}")

        println(tangoFromNodeId)
    }

}