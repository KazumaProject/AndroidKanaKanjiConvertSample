package com.kazumaproject.androidkanakanjiconvertsample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kazumaproject.kana_kanji_converter.local.connection_id.entity.ConnectionID
import com.kazumaproject.kana_kanji_converter.system.SystemDictionaryBuilder
import com.kazumaproject.kana_kanji_converter.system.connection_id.ConnectionIdDatabaseBuilder
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val systemDictionaryBuilder = SystemDictionaryBuilder(this)
        val connectionIdDatabaseBuilder = ConnectionIdDatabaseBuilder(this)
        buildSystemDictionary(systemDictionaryBuilder)
        //checkSystemDictionaryDatabaseSize(systemDictionaryBuilder)
        buildConnectionIdDatabase(connectionIdDatabaseBuilder)
        //checkConnectionIdListSize(connectionIdDatabaseBuilder)
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

    private fun checkSystemDictionaryDatabaseSize(
        systemDictionaryBuilder: SystemDictionaryBuilder
    ) = lifecycleScope.launch {
        Log.d("system dictionary size","${systemDictionaryBuilder.getAllDictionaryList().size}")
    }

    private fun buildConnectionIdDatabase(
        connectionIdDatabaseBuilder: ConnectionIdDatabaseBuilder
    ) = lifecycleScope.launch {
        connectionIdDatabaseBuilder.insertConnectionIdFromFile("connection_id/connection_single_column.txt")
    }

    private fun checkConnectionIdListSize(
        connectionIdDatabaseBuilder: ConnectionIdDatabaseBuilder
    ) = lifecycleScope.launch {
        val reader = BufferedReader(InputStreamReader(assets.open("connection_id/connection_single_column.txt")))
        val lines = reader.readLines().mapIndexed { index, s ->
            ConnectionID(
                cID = index,
                cost = s.toInt()
            )
        }
        val list = connectionIdDatabaseBuilder.getConnectionIdList()

        val sum = lines + list
        val unCommon = sum.groupBy { it.cID }
            .filter { it.value.size == 1 }
            .flatMap { it.value }
        Log.d("connection id list", "$unCommon")

    }

}