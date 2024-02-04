package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import com.kazumaproject.kana_kanji_converter.models.DictionaryEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.trie4j.patricia.TailPatriciaTrie
import java.io.BufferedReader
import java.io.InputStreamReader

class SystemDictionaryBuilder (private val context: Context) {

    private var tailPatriciaTrie: TailPatriciaTrie = TailPatriciaTrie()

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
    ): TailPatriciaTrie{
        val list = groupAllDictionaries(
            dictionaries,
            singleKanjiFileName
        )
        list.forEach {
            tailPatriciaTrie.insert(it.key)
        }
        return tailPatriciaTrie
    }

}