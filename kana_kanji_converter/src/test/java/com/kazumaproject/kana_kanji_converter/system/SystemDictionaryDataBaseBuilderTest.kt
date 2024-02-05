package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import androidx.room.Room
import com.kazumaproject.kana_kanji_converter.local.DictionaryDao
import com.kazumaproject.kana_kanji_converter.local.DictionaryDatabaseConverter
import com.kazumaproject.kana_kanji_converter.local.SystemDictionaryDatabase
import com.kazumaproject.kana_kanji_converter.local.entity.D
import com.kazumaproject.kana_kanji_converter.local.entity.DictionaryDatabaseEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(assetDir = "src/test/assets")
class SystemDictionaryDataBaseBuilderTest {
    private lateinit var context: Context
    private lateinit var systemDicDatabase: SystemDictionaryDatabase
    private lateinit var dictionaryDao: DictionaryDao
    private lateinit var systemDictionaryBuilder: SystemDictionaryBuilder

    @Before
    fun  setUpSystemDictionaryDatabase(){
        context = RuntimeEnvironment.getApplication()
        systemDictionaryBuilder = SystemDictionaryBuilder(context)
        val moshi = Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        systemDicDatabase = Room
            .inMemoryDatabaseBuilder(
            context,
            SystemDictionaryDatabase::class.java
        )
            .addTypeConverter(DictionaryDatabaseConverter(moshi))
            .allowMainThreadQueries()
            .build()
        dictionaryDao = systemDicDatabase.dictionaryDao
    }

    @After
    fun closeSystemDictionaryDatabase(){
        systemDicDatabase.close()
    }

    @Test
    fun `Test insert system dictionary`() = runBlocking {
        val test = DictionaryDatabaseEntity(1234, listOf())
        dictionaryDao.insertDictionaryEntry(test)
        println("${dictionaryDao.getDictionaryEntryList()}")
    }

}