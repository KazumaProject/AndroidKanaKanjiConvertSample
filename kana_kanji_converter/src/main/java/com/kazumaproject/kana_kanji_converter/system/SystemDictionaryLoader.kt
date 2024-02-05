package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import androidx.room.Room
import com.kazumaproject.kana_kanji_converter.local.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.DictionaryDatabaseConverter
import com.kazumaproject.kana_kanji_converter.local.SystemDictionaryDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class SystemDictionaryLoader(context: Context) {

    private var systemDictionaryDatabase: SystemDictionaryDatabase
    private var dictionaryDao: DictionaryDao

    init{
        val moshi = Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        systemDictionaryDatabase = Room
            .databaseBuilder(context, SystemDictionaryDatabase::class.java,"system_dictionary")
            .addTypeConverter(DictionaryDatabaseConverter(moshi))
            .allowMainThreadQueries()
            .build()
        dictionaryDao = systemDictionaryDatabase.dictionaryDao
    }

}