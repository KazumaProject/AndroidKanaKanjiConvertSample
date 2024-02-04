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
        val b = mutableListOf<DictionaryEntry>()
        dictionaries.forEach {  fileName ->
            val list= readSingleDictionaryFile(fileName)
            list.forEach {
                b.add(it)
            }
        }
        return@async b
    }.await()

    suspend fun groupAllDictionaries(dictionaries: List<String>) =
        CoroutineScope(Dispatchers.IO).async {
            return@async readDictionaryFiles(dictionaries).groupBy { it.surface }
        }.await()

    suspend fun createYomiTrie(dictionaries: List<String>): TailPatriciaTrie{
        val list = groupAllDictionaries(dictionaries)
        list.forEach {
            tailPatriciaTrie.insert(it.key)
        }
        return tailPatriciaTrie
    }

}