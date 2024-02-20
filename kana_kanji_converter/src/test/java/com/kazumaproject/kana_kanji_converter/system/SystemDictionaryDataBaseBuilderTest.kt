package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import androidx.room.Room
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.SystemDictionaryDatabase
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.entity.Tango
import com.kazumaproject.kana_kanji_converter.model.TokenEntry
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import com.kazumaproject.trie4j.louds.TailLOUDSTrie
import org.junit.Assert.*
import com.kazumaproject.trie4j.patricia.TailPatriciaTrie
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(assetDir = "src/test/assets")
class SystemDictionaryDataBaseBuilderTest {
    private lateinit var context: Context
    private lateinit var systemDictionaryBuilder: SystemDictionaryBuilder

    private lateinit var systemDicDatabase: SystemDictionaryDatabase
    private lateinit var dictionaryDao: DictionaryDao

    @Before
    fun setUpSystemDictionary(){
        context = RuntimeEnvironment.getApplication()
        systemDictionaryBuilder = SystemDictionaryBuilder(context)

        systemDicDatabase = Room
            .inMemoryDatabaseBuilder(
                context,
                SystemDictionaryDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        dictionaryDao = systemDicDatabase.dictionaryDao
    }

    @After
    fun closeDatabase(){
        systemDicDatabase.close()
    }

    /** 263725 **/
    @Test
    fun `Test build yomi dic`() = runBlocking{
        val list = systemDictionaryBuilder.groupAllDictionaries(
            listOf(
                "dictionaries/dictionary00.txt",
                "dictionaries/dictionary01.txt",
            ),
            "single_kanji/single_kanji.tsv"
        )
        val trie = TailPatriciaTrie()
        launch {
            list.keys.forEach {
                trie.insert(it)
                println("insert: $it")
            }
        }.join()
        val yomiTrie = TailLOUDSTrie(trie)
        println("${list.size} ${yomiTrie.size()} ${yomiTrie.nodeSize()}")
        assertEquals(list.size,yomiTrie.size())
    }

    @Test
    fun `Test build tango dic`() = runBlocking{
        val trie = TailPatriciaTrie()
        val list = systemDictionaryBuilder.groupAllDictionaries(
            listOf(
                "dictionaries/dictionary00.txt",
                "dictionaries/dictionary01.txt",
            ),
            "single_kanji/single_kanji.tsv"
        )
        val expected = list.flatMap { it.value }.size
        list.flatMap {
            it.value.map { v -> v.afterConversion }
        }.forEachIndexed { index, s ->
            println("insert: $index $s")
            trie.insert(s)
        }
        val tangoTrie = TailLOUDSTrie(trie)
        println("${list.size} ${list.values.size} ${tangoTrie.nodeSize()} ${tangoTrie.size()} ${trie.nodeSize()} ${trie.size()}")
        assertEquals(expected,tangoTrie.nodeSize())
    }

    @Test
    fun `Test build token def with database`() = runBlocking{

        val atomicInteger = AtomicInteger(0)

        val list = systemDictionaryBuilder.groupAllDictionaries(
            listOf(
                "dictionaries/dictionary00.txt",
                "dictionaries/dictionary01.txt",
            ),
            "single_kanji/single_kanji.tsv"
        )

        val trie = TailPatriciaTrie()
        launch {
            list.keys.forEach {
                trie.insert(it)
                println("insert: $it")
            }
        }.join()
        val yomiTrie = TailLOUDSTrie(trie)

        val tokenArray: ArrayList<List<TokenEntry>> = arrayListOf()

        for (i in 0 until yomiTrie.nodeSize()){
            tokenArray.add(emptyList())
        }

        launch {
            list.forEach { entry ->
                val index = yomiTrie.getNodeId(entry.key)
                val tokenEntryList: List<TokenEntry> = entry.value.map { dictionaryEntry ->
                    val id = atomicInteger.incrementAndGet()
                    launch {
                        val tango = Tango(
                            dictionaryEntry.afterConversion,
                            id
                        )
                        dictionaryDao.insertTango(tango)
                        println("insert tango: $tango")
                    }.join()

                    return@map TokenEntry(
                        leftId = dictionaryEntry.leftID.toShort(),
                        rightId = dictionaryEntry.rightID.toShort(),
                        cost = dictionaryEntry.wordCost.toShort(),
                        tangoId = id
                    )
                }
                tokenArray[index] = tokenEntryList
            }
        }.join()


        println("token array size: ${tokenArray.size}")

        val queryText = "もうしこみまどぐち"
        val yomiNodeId = yomiTrie.getNodeId(queryText)
        val tokens = tokenArray[yomiNodeId]

        tokens.forEach { token ->
            val tangoFromId = dictionaryDao.getTangoListFromTangoId(token.tangoId)
            println("$tangoFromId")
        }
    }

}