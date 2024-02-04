package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.InputStreamReader

@RunWith(RobolectricTestRunner::class)
@Config(assetDir = "src/test/assets")
class SystemDictionaryBuilderTest {

    private lateinit var context: Context
    private lateinit var systemDictionaryBuilder: SystemDictionaryBuilder

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        systemDictionaryBuilder = SystemDictionaryBuilder(context)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `Test readSingleDictionaryFile`() = runBlocking{
        val dic0 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary00.txt")
        val dic1 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary01.txt")
        val dic2 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary02.txt")
        val dic3 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary03.txt")
        val dic4 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary04.txt")
        val dic5 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary05.txt")
        val dic6 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary06.txt")
        val dic7 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary07.txt")
        val dic8 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary08.txt")
        val dic9 = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/dictionary09.txt")
        val suffix = systemDictionaryBuilder.readSingleDictionaryFile("dictionaries/suffix.txt")

        println("read size dic0: ${dic0.size} ${dic0.first().afterConversion} ${dic0.last().afterConversion}")
        println("read size dic1: ${dic1.size} ${dic1.first().afterConversion} ${dic1.last().afterConversion}")
        println("read size dic2: ${dic2.size} ${dic2.first().afterConversion} ${dic2.last().afterConversion}")
        println("read size dic3: ${dic3.size} ${dic3.first().afterConversion} ${dic3.last().afterConversion}")
        println("read size dic4: ${dic4.size} ${dic4.first().afterConversion} ${dic4.last().afterConversion}")
        println("read size dic5: ${dic5.size} ${dic5.first().afterConversion} ${dic5.last().afterConversion}")
        println("read size dic6: ${dic6.size} ${dic6.first().afterConversion} ${dic6.last().afterConversion}")
        println("read size dic7: ${dic7.size} ${dic7.first().afterConversion} ${dic7.last().afterConversion}")
        println("read size dic8: ${dic8.size} ${dic8.first().afterConversion} ${dic8.last().afterConversion}")
        println("read size dic9: ${dic9.size} ${dic9.first().afterConversion} ${dic9.last().afterConversion}")
        println("read size suffix: ${suffix.size} ${suffix.first().afterConversion} ${suffix.last().afterConversion}")

        val expectedSizeDic0 = InputStreamReader(
            context.assets.open("dictionaries/dictionary00.txt")
        ).readLines().size
        val expectedSizeDic1 = InputStreamReader(
            context.assets.open("dictionaries/dictionary01.txt")
        ).readLines().size
        val expectedSizeDic2 = InputStreamReader(
            context.assets.open("dictionaries/dictionary02.txt")
        ).readLines().size
        val expectedSizeDic3 = InputStreamReader(
            context.assets.open("dictionaries/dictionary03.txt")
        ).readLines().size
        val expectedSizeDic4 = InputStreamReader(
            context.assets.open("dictionaries/dictionary04.txt")
        ).readLines().size
        val expectedSizeDic5 = InputStreamReader(
            context.assets.open("dictionaries/dictionary05.txt")
        ).readLines().size
        val expectedSizeDic6 = InputStreamReader(
            context.assets.open("dictionaries/dictionary06.txt")
        ).readLines().size
        val expectedSizeDic7 = InputStreamReader(
            context.assets.open("dictionaries/dictionary07.txt")
        ).readLines().size
        val expectedSizeDic8 = InputStreamReader(
            context.assets.open("dictionaries/dictionary08.txt")
        ).readLines().size
        val expectedSizeDic9 = InputStreamReader(
            context.assets.open("dictionaries/dictionary09.txt")
        ).readLines().size
        val expectedSizeSuffix = InputStreamReader(
            context.assets.open("dictionaries/suffix.txt")
        ).readLines().size

        /**
         * check dictionary 0 ~ 9 size
         * 0 ~ 8: 154368
         * 9: 153465
         * suffix: 1737
         *                              **/
        assertEquals(expectedSizeDic0,dic0.size)
        assertEquals(expectedSizeDic1,dic1.size)
        assertEquals(expectedSizeDic2,dic2.size)
        assertEquals(expectedSizeDic3,dic3.size)
        assertEquals(expectedSizeDic4,dic4.size)
        assertEquals(expectedSizeDic5,dic5.size)
        assertEquals(expectedSizeDic6,dic6.size)
        assertEquals(expectedSizeDic7,dic7.size)
        assertEquals(expectedSizeDic8,dic8.size)
        assertEquals(expectedSizeDic9, dic9.size)
        /** check suffix size **/
        assertEquals(expectedSizeSuffix, suffix.size)
    }

    @Test
    fun `Test readSingleKanji`() = runBlocking {
        val singleKanji = systemDictionaryBuilder
            .readSingleKanji("single_kanji/single_kanji.tsv")
        println("test single kanji: $singleKanji")
    }

    @Test
    fun `Test readDictionaryFiles`() = runBlocking{
        val allDictionaries = systemDictionaryBuilder.readDictionaryFiles(
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
            )
        )
        println("all dictionaries size: ${allDictionaries.size}")
        /** total 1545414 **/
        assertEquals((154368 * 9 + 154365 + 1737), allDictionaries.size)
    }

    @Test
    fun `Test combineSingleKanjiAndDictionaries`() = runBlocking {
        val list = systemDictionaryBuilder.combineSingleKanjiAndDictionaries(
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
        println("combine file: ${list.size}")
        val expected = 1569110
        assertEquals(expected, list.size)
    }

    @Test
    fun `Test groupAllDictionaries`() = runBlocking {
        val groupedDictionaries = systemDictionaryBuilder.groupAllDictionaries(
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
        println("group system dictionaries: ${groupedDictionaries.size}")
        val expected = 1025057
        assertEquals(expected, groupedDictionaries.size)
    }

    @Test
    fun `Test createYomiTrie`() = runBlocking {
        val tailTrie = systemDictionaryBuilder.createYomiTrie(
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
        println("trie test: ${tailTrie.size()}")
        assertEquals(1025057, tailTrie.size())
    }



}