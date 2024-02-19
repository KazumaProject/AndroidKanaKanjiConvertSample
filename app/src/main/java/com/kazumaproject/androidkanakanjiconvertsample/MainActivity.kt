package com.kazumaproject.androidkanakanjiconvertsample

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.kazumaproject.kana_kanji_converter.graph.GraphBuilder
import com.kazumaproject.kana_kanji_converter.system.SystemDictionaryBuilder
import com.kazumaproject.kana_kanji_converter.system.SystemDictionaryLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private var convertHiraganaToKanjiJob: Job? = null
    private var adapter: ArrayAdapter<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val systemDictionaryBuilder = SystemDictionaryBuilder(this)
        val graphBuilder = GraphBuilder()
        val systemDictionaryLoader = SystemDictionaryLoader(this)
        buildSystemDictionary(systemDictionaryBuilder)
        //checkSystemDictionaryDatabaseSize(systemDictionaryBuilder)
        //checkConnectionIdListSize(systemDictionaryBuilder)

        val listView = findViewById<ListView>(R.id.sample_list_view)
        val editText = findViewById<EditText>(R.id.sample_edit_text)
//        Log.d("Loading connectionIds","loading...")
//        val connectionIds = systemDictionaryBuilder.getConnectionIds()
//        Log.d("Loading connectionIds","finished... ${connectionIds.size}")
//
//        editText.addTextChangedListener {
//
//            convertHiraganaToKanjiJob?.cancel()
//
//            convertHiraganaToKanjiJob = lifecycleScope.launch {
//                val yomiTrie = systemDictionaryLoader.loadYomiDic()
//                val convertedResultInList = graphBuilder.constructGraphAndGetResult(
//                    queryText = it.toString(),
//                    yomiTrie = yomiTrie,
//                    systemDictionaryBuilder = systemDictionaryBuilder,
//                    connectionIds
//                )
//                adapter = ArrayAdapter(this@MainActivity,android.R.layout.simple_list_item_1,convertedResultInList)
//                listView.adapter = adapter
//            }
//        }

    }

    private fun buildSystemDictionary(
        systemDictionaryBuilder: SystemDictionaryBuilder
    ) = lifecycleScope.launch{
        launch {
            systemDictionaryBuilder.createSystemDictionaryDatabaseAndSaveTrie(
                listOf(
                    "dictionaries/dictionary00.txt",
                    "dictionaries/dictionary01.txt",
                    "dictionaries/dictionary02.txt",
                    "dictionaries/dictionary03.txt",
                    "dictionaries/dictionary04.txt",
                    "dictionaries/dictionary05.txt",
                    "dictionaries/dictionary06.txt",
                    "dictionaries/dictionary07.txt",
                    "dictionaries/dictionary08.txt",
                    "dictionaries/dictionary09.txt",
                    "dictionaries/suffix.txt",
                ),
                "single_kanji/single_kanji.tsv"
            )
        }.join()
        systemDictionaryBuilder.closeSystemDictionaryDatabase()
    }

    /** 1025057 **/
    private fun checkSystemDictionaryDatabaseSize(
        systemDictionaryBuilder: SystemDictionaryBuilder
    ) = lifecycleScope.launch {
        Log.d("system dictionary size","${systemDictionaryBuilder.getAllDictionaryList().size}")
    }

    /** 7086244 **/
    private fun checkConnectionIdListSize(
        systemDictionaryBuilder: SystemDictionaryBuilder
    ) = lifecycleScope.launch {
        val connectionIds = systemDictionaryBuilder.getConnectionIds()
        println("${connectionIds.size}")
    }

}