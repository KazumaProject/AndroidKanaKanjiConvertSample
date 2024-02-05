package com.kazumaproject.androidkanakanjiconvertsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.kazumaproject.kana_kanji_converter.system.SystemDictionaryBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun buildSystemDictionary(
        systemDictionaryBuilder: SystemDictionaryBuilder
    ) = lifecycleScope.launch{
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
        systemDictionaryBuilder.closeSystemDictionaryDatabase()
    }

}