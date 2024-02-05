package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.kazumaproject.kana_kanji_converter.local.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.DictionaryDatabaseConverter
import com.kazumaproject.kana_kanji_converter.local.SystemDictionaryDatabase
import com.kazumaproject.kana_kanji_converter.local.entity.D
import com.kazumaproject.kana_kanji_converter.local.entity.DictionaryDatabaseEntity
import com.kazumaproject.kana_kanji_converter.model.DictionaryEntry
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.trie4j.louds.TailLOUDSTrie
import org.trie4j.patricia.TailPatriciaTrie
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.ObjectOutputStream

class SystemDictionaryBuilder (private val context: Context) {

    private var tailPatriciaTrie: TailPatriciaTrie = TailPatriciaTrie()
    private var systemDictionaryDatabase: SystemDictionaryDatabase
    private var dictionaryDao: DictionaryDao

    init{
        val moshi = Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        systemDictionaryDatabase = Room
            .databaseBuilder(context,SystemDictionaryDatabase::class.java,"system_dictionary")
            .addTypeConverter(DictionaryDatabaseConverter(moshi))
            .allowMainThreadQueries()
            .build()
        dictionaryDao = systemDictionaryDatabase.dictionaryDao
    }

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
        dictionaries.forEach {  fileName ->
            val list= readSingleDictionaryFile(fileName)
            list.forEach {
                tempList.add(it)
            }
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
        singleKanjiInMap.forEach { entry ->
            entry.value.forEach { singleKanji ->
                tempList.add(
                    DictionaryEntry(
                        surface = entry.key,
                        leftID = 1916,
                        rightID = 1916,
                        wordCost = 3000,
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
    ) =
        CoroutineScope(Dispatchers.IO).async {
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
        list.forEach {
            tailPatriciaTrie.insert(it.key)
            Log.d("insert to trie",it.key)
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
    ){
        val yomiTrie = createYomiTrie(
            dictionaries,
            singleKanjiFileName
        )
        val groupedList = groupAllDictionaries(
            dictionaries,
            singleKanjiFileName
        )
        groupedList.forEach { entry ->
            val nodeId = yomiTrie.getNodeId(entry.key)
            val features = entry.value.map {
                D(
                    l = it.leftID,
                    r = it.rightID,
                    c = it.wordCost,
                    t = it.afterConversion
                )
            }
            val dictionaryDatabaseEntity = DictionaryDatabaseEntity(
                nodeId = nodeId,
                features = features
            )
            dictionaryDao.insertDictionaryEntry(dictionaryDatabaseEntity)
            Log.d("insert dictionary entry","$dictionaryDatabaseEntity")
        }
        saveTrieInInternalStorage(yomiTrie,"yomi.dic")
    }

    private suspend fun saveTrieInInternalStorage(
        yomiTrie: TailLOUDSTrie,
        outputName: String
    ){
        Log.d("yomi.dic","started to create yomi.dic")
        try {
            val out = context.openFileOutput(outputName, Context.MODE_PRIVATE)
            withContext(Dispatchers.IO) {
                yomiTrie.writeExternal(ObjectOutputStream(out))
            }
        }catch (e: Exception){
            Log.e("system dic",e.stackTraceToString())
        }
        Log.d("yomi.dic","finished to create yomi.dic")
    }

    fun closeSystemDictionaryDatabase(){
        systemDictionaryDatabase.close()
    }

}