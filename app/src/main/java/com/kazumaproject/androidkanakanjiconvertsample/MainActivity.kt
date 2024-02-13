package com.kazumaproject.androidkanakanjiconvertsample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kazumaproject.kana_kanji_converter.system.SystemDictionaryBuilder
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
        //buildSystemDictionary(systemDictionaryBuilder)
        //checkSystemDictionaryDatabaseSize(systemDictionaryBuilder)
        checkConnectionIdListSize(systemDictionaryBuilder)
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

}