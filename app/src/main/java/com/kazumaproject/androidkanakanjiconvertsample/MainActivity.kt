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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val systemDictionaryBuilder = SystemDictionaryBuilder(this)
        val graphBuilder = GraphBuilder()
        val systemDictionaryLoader = SystemDictionaryLoader(this)
        //buildSystemDictionary(systemDictionaryBuilder)
        //checkSystemDictionaryDatabaseSize(systemDictionaryBuilder)
        //checkConnectionIdListSize(systemDictionaryBuilder)

        val listView = findViewById<ListView>(R.id.sample_list_view)
        val editText = findViewById<EditText>(R.id.sample_edit_text)

        convertHiragana(
            graphBuilder, systemDictionaryBuilder, systemDictionaryLoader, editText, listView
        )
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
        withContext(Dispatchers.IO){
            val list = systemDictionaryBuilder.getAllConnectionIds()
            val reader = BufferedReader(InputStreamReader(this@MainActivity.assets.open("connection_id/connection_single_column.txt")))
            val connectionIdsFromText = reader.readLines()
            Log.d("connection id list", "${list.size} ${connectionIdsFromText.size}")
            Log.d("connection id list", "${list[10]} ${connectionIdsFromText[10]}\n" +
                    "${list[100]} ${connectionIdsFromText[100]}\n" +
                    "${list[1000]} ${connectionIdsFromText[1000]}\n" +
                    "${list[10000]} ${connectionIdsFromText[10000]}\n")
        }
    }

    private fun convertHiragana(
        graphBuilder: GraphBuilder,
        systemDictionaryBuilder: SystemDictionaryBuilder,
        systemDictionaryLoader: SystemDictionaryLoader,
        editText: EditText,
        listView: ListView,
    ) = lifecycleScope.launch {
        val yomiTrie = systemDictionaryLoader.loadYomiDic()

        editText.addTextChangedListener {
            CoroutineScope(Dispatchers.Main).launch {
                val convertedResultInList = graphBuilder.constructGraphAndGetResult(
                    queryText = it.toString(),
                    yomiTrie = yomiTrie,
                    systemDictionaryBuilder = systemDictionaryBuilder
                )
                val adapter = ArrayAdapter(this@MainActivity,android.R.layout.simple_list_item_1,convertedResultInList)
                listView.adapter = adapter
            }
        }

    }

}