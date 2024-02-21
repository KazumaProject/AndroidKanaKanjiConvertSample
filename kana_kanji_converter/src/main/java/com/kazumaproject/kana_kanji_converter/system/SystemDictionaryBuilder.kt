package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.system_dictionary.SystemDictionaryDatabase
import com.kazumaproject.kana_kanji_converter.model.DictionaryEntry
import com.kazumaproject.kana_kanji_converter.model.TokenEntry
import com.kazumaproject.kana_kanji_converter.other.hiraToKata
import com.kazumaproject.trie4j.louds.MapTailLOUDSTrie
import com.kazumaproject.trie4j.louds.TailLOUDSTrie
import com.kazumaproject.trie4j.patricia.MapTailPatriciaTrie
import com.kazumaproject.trie4j.patricia.TailPatriciaTrie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.coroutines.cancellation.CancellationException

class SystemDictionaryBuilder (private val context: Context) {

    private var tailPatriciaTrie: TailPatriciaTrie = TailPatriciaTrie()
    private var systemDictionaryDatabase: SystemDictionaryDatabase
    private var dictionaryDao: DictionaryDao
    private var systemDictionaryDatabaseForPrepopulate: SystemDictionaryDatabase
    private var dictionaryDaoForPrepopulate: DictionaryDao

    init{
        systemDictionaryDatabaseForPrepopulate = Room
            .databaseBuilder(context, SystemDictionaryDatabase::class.java,"system_dictionary")
            .fallbackToDestructiveMigration()
            .build()
        dictionaryDaoForPrepopulate = systemDictionaryDatabaseForPrepopulate.dictionaryDao

        systemDictionaryDatabase = Room
            .databaseBuilder(context, SystemDictionaryDatabase::class.java,"system_dictionary")
            .build()
        dictionaryDao = systemDictionaryDatabase.dictionaryDao

    }
    suspend fun getAllDictionaryList() = dictionaryDaoForPrepopulate.getTangoList()

    suspend fun readSingleDictionaryFile(fileName: String) =
        CoroutineScope(Dispatchers.IO).async {
        val reader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
        return@async reader.readLines().map {
            val splitStr = it.split("\\t".toRegex())
            DictionaryEntry(
                surface = splitStr[0],
                leftID = splitStr[1].toInt(),
                rightID = splitStr[2].toInt(),
                wordCost = splitStr[3].toInt(),
                afterConversion = splitStr[4]
            )
        }
    }.await()

    suspend fun readDictionaryFiles(dictionaries: List<String>): List<DictionaryEntry> =
        CoroutineScope(Dispatchers.IO).async{
        val tempList = mutableListOf<DictionaryEntry>()
        for (fileName in dictionaries){
            val list= readSingleDictionaryFile(fileName)
            for (entry in list) tempList.add(entry)

        }
        return@async tempList
    }.await()

    suspend fun readSingleKanji(fileName: String) = CoroutineScope(Dispatchers.IO).async {
        val reader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
        val tempList = reader.readLines().map { str1 ->
            str1.split(",".toRegex()).flatMap { str2 ->
                str2.split("\\t".toRegex())
            }
        }
        return@async tempList.associate {
            it[0] to it[1].toList()
        }
    }.await()

    private suspend fun convertSingleKanjiToDictionaryEntry(fileName: String) = CoroutineScope(Dispatchers.IO).async{
        val singleKanjiInMap = readSingleKanji(fileName)
        val tempList = mutableListOf<DictionaryEntry>()
        for (entry in singleKanjiInMap){
            for (singleKanji in entry.value){
                tempList.add(
                    DictionaryEntry(
                        surface = entry.key,
                        leftID = 1916,
                        rightID = 1916,
                        wordCost = 5000,
                        afterConversion = singleKanji.toString()
                    )
                )
            }
        }
        return@async tempList.toList()
    }.await()

    suspend fun combineSingleKanjiAndDictionaries(
        dictionaries: List<String>,
        singleKanjiFileName: String
    ) = CoroutineScope(Dispatchers.IO).async{
        val loadedDictionaries = readDictionaryFiles(dictionaries)
        val singleKanji = convertSingleKanjiToDictionaryEntry(singleKanjiFileName)
        return@async loadedDictionaries + singleKanji
    }.await()

    suspend fun groupAllDictionaries(
        dictionaries: List<String>,
        singleKanjiFileName: String
    ) = CoroutineScope(Dispatchers.IO).async {
            return@async combineSingleKanjiAndDictionaries(
                dictionaries,
                singleKanjiFileName
            ).groupBy { it.surface }
        }.await()

    suspend fun createYomiTrie(
        dictionaries: List<String>,
        singleKanjiFileName: String
    ): TailLOUDSTrie{
        val list = groupAllDictionaries(
            dictionaries,
            singleKanjiFileName
        )
        for (entry in list){
            tailPatriciaTrie.insert(entry.key)
            Log.d("insert to trie",entry.key)
        }
        return TailLOUDSTrie(tailPatriciaTrie)
    }

    /**
     *
     * yomi.dic: data/data/package_name/files/yomi.dic
     * database files: data/data/package_name/databases/
     *
     * **/
    suspend fun createSystemDictionaryDatabaseAndSaveTrie(
        dictionaries: List<String>,
        singleKanjiFileName: String
    ) = CoroutineScope(Dispatchers.IO).launch{

        val startTime = System.currentTimeMillis()
        val yomiTrie = createYomiTrie(
            dictionaries,
            singleKanjiFileName
        )
        val groupedList = groupAllDictionaries(
            dictionaries,
            singleKanjiFileName
        )

        val tokenArray: ArrayList<List<TokenEntry>> = arrayListOf()
        for (i in 0 until yomiTrie.nodeSize()){
            tokenArray.add(emptyList())
        }

        launch {
            groupedList.forEach { entry ->
                val index = yomiTrie.getNodeId(entry.key)

                println("inserted in token array trie")
                val tokenEntryList: List<TokenEntry> = entry.value.map { dictionaryEntry ->
                    return@map TokenEntry(
                        leftId = dictionaryEntry.leftID.toShort(),
                        rightId = dictionaryEntry.rightID.toShort(),
                        cost = dictionaryEntry.wordCost.toShort(),
                        tango = if (dictionaryEntry.afterConversion == dictionaryEntry.surface || dictionaryEntry.surface.hiraToKata() == dictionaryEntry.afterConversion) null else dictionaryEntry.afterConversion,
                    )
                }
                tokenArray[index] = tokenEntryList
            }
        }.join()

        Log.d("build yomi.dic","started...")
        saveYomiTrieInInternalStorage(yomiTrie).join()
        Log.d("build yomi.dic","finished...")

        Log.d("build token.def","started...")
        launch {
            val outputStream = context.openFileOutput("token.def",Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(outputStream)
            try {
                objectOutputStream.apply {
                    writeObject(tokenArray)
                    flush()
                    close()
                }
            }catch (e: Exception){
                if (e is CancellationException) throw e
            }
        }.join()
        Log.d("build token.def","finished...")

        val endTime = System.currentTimeMillis()
        val elapsedTime = (endTime - startTime) / 1000
        Log.d("Execution time to build system dictionary","$elapsedTime seconds")
    }

    private fun saveYomiTrieInInternalStorage(
        yomiTrie: TailLOUDSTrie,
    ) = CoroutineScope(Dispatchers.IO).launch {
        Log.d("yomi.dic","started to create yomi.dic")
        try {
            val out = context.openFileOutput("yomi.dic", Context.MODE_PRIVATE)
            yomiTrie.writeExternal(ObjectOutputStream(out))
        }catch (e: Exception){
            Log.e("system dic",e.stackTraceToString())
        }
        Log.d("yomi.dic","finished to create yomi.dic")
    }

    private fun saveTangoTrieInInternalStorage(
        tangoTrie: MapTailLOUDSTrie<String>,
    ) = CoroutineScope(Dispatchers.IO).launch {
        Log.d("tango.dic","started to create tango.dic")
        try {
            val out = context.openFileOutput("tango.dic", Context.MODE_PRIVATE)
            tangoTrie.writeExternal(ObjectOutputStream(out))
        }catch (e: Exception){
            Log.e("system dic",e.stackTraceToString())
        }
        Log.d("tango.dic","finished to create tango.dic")
    }

    private fun saveTokenArrayInInternalStorage(
        tokenTrie: MapTailLOUDSTrie<List<TokenEntry>>,
    ) = CoroutineScope(Dispatchers.IO).launch {
        Log.d("token.dic","started to create tango.def")
        try {
            val out = context.openFileOutput("token.def", Context.MODE_PRIVATE)
            tokenTrie.writeExternal(ObjectOutputStream(out))
        }catch (e: Exception){
            Log.e("system dic",e.stackTraceToString())
        }
        Log.d("token.dic","finished to create tango.def")
    }

    fun closeSystemDictionaryDatabase(){
        systemDictionaryDatabase.close()
    }

    fun buildConnectionIdWithDoubleTrie() = CoroutineScope(Dispatchers.IO).launch{
        println("started to build connection.def")
        val dic = MapTailPatriciaTrie<Short>()
        val reader = BufferedReader(InputStreamReader(context.assets.open("connection_id/connection_single_column.txt"))).readLines()
        for (i in reader.indices){
            launch {
                val key = i.toString()
                val value = reader[i].toShort()
                dic.insert(key,value)
                println("insert $key $value")
            }.join()
        }
        val loudsTrie = TailLOUDSTrie(dic)
        val out = context.openFileOutput("connection.def",Context.MODE_PRIVATE)
        loudsTrie.writeExternal(ObjectOutputStream(out))
        println("finished to build connection.def")
    }

    fun getConnectionIdsFromText(): List<String> {
        val reader = BufferedReader(InputStreamReader(context.assets.open("connection_id/connection_single_column.txt")))
        return reader.readLines()
    }

    fun getSystemDictionaryDao(): DictionaryDao = dictionaryDaoForPrepopulate

    fun getConnectionIds(): IntArray {
        val reader =
            BufferedReader(InputStreamReader(context.assets.open("connection_id/connection_single_column.txt"))).readLines()
        return reader.map { it.toInt() }.toIntArray()
    }

    @Suppress("UNCHECKED_CAST")
    fun readConnectionIdsFromBinaryFile(): ArrayList<Short> {
        val ois = ObjectInputStream(context.assets.open("connection_id/connectionIds.def"))
        return ois.readObject() as ArrayList<Short>
    }

}