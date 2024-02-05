package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kazumaproject.kana_kanji_converter.local.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.DictionaryDatabaseConverter
import com.kazumaproject.kana_kanji_converter.local.SystemDictionaryDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.trie4j.louds.TailLOUDSTrie
import java.io.ObjectInputStream

class SystemDictionaryLoader(private val context: Context) {

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
            .createFromAsset("system_dictionary_database/system_dictionary")
            .addMigrations(object : Migration(5,6){
                override fun migrate(db: SupportSQLiteDatabase) {
                }

            })
            .build()
        dictionaryDao = systemDictionaryDatabase.dictionaryDao
    }

    suspend fun loadYomiDic():TailLOUDSTrie {
        val lt = TailLOUDSTrie()
        lt.readExternal(withContext(Dispatchers.IO) {
            ObjectInputStream(context.assets.open("system_trie/yomi.dic"))
        })
        return lt
    }

}