package com.kazumaproject.androidkanakanjiconvertsample

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.kazumaproject.Louds.Converter
import com.kazumaproject.Louds.LOUDS
import com.kazumaproject.Louds.with_term_id.ConverterWithTermId
import com.kazumaproject.Louds.with_term_id.LOUDSWithTermId
import com.kazumaproject.connection_id.ConnectionIdBuilder
import com.kazumaproject.dictionary.DicUtils
import com.kazumaproject.dictionary.TokenArray
import com.kazumaproject.dictionary.models.Dictionary
import com.kazumaproject.converter.engine.KanaKanjiEngine
import com.kazumaproject.converter.graph.GraphBuilder
import com.kazumaproject.hiraToKata
import com.kazumaproject.prefix.PrefixTree
import com.kazumaproject.prefix.with_term_id.PrefixTreeWithTermId
import com.kazumaproject.viterbi.FindPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val kanaKanjiEngine = KanaKanjiEngine()
        kanaKanjiEngine.buildEngine(this)

        val listView = findViewById<ListView>(R.id.sample_list_view)
        val editText = findViewById<EditText>(R.id.sample_edit_text)

        var arrayAdapter: ArrayAdapter<*>

        editText.addTextChangedListener {

            CoroutineScope(Dispatchers.Main).launch {
                if (it.toString().length >= 2){
                    val result = async(Dispatchers.IO) {
                        return@async kanaKanjiEngine.nBestPath(it.toString(),5)
                    }
                    arrayAdapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1, result.await()
                    )
                    listView.adapter = arrayAdapter
                }else{
                    arrayAdapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1, listOf<String>()
                    )
                    listView.adapter = arrayAdapter
                }
            }
        }

    }

    private fun buildPOSTable(
        outputStream: ObjectOutputStream,
        outputStream2: ObjectOutputStream
    ){
        val fileList: List<String> = listOf(
            "/dictionary00.txt",
            "/dictionary01.txt",
            "/dictionary02.txt",
            "/dictionary03.txt",
            "/dictionary04.txt",
            "/dictionary05.txt",
            "/dictionary06.txt",
            "/dictionary07.txt",
            "/dictionary08.txt",
            "/dictionary09.txt",
            "/suffix.txt",
            "/domain.txt",
            "/era.txt"
        )
        val tokenArray = TokenArray()
        tokenArray.buildPOSTable(fileList,outputStream)
        tokenArray.buildPOSTableWithIndex(fileList,outputStream2)
    }

    private fun buildTriesAndTokenArray(
        objectInputStreamForReadPOSTable: ObjectInputStream,
        objectInputStreamForReadPOSTableWithIndex: ObjectInputStream
    ){
        val yomiTree = PrefixTreeWithTermId()
        val tangoTree = PrefixTree()

        val dicUtils = DicUtils()

        val mode = 3
        val tempList: MutableList<Dictionary> = mutableListOf()

        val list = when(mode){
            0 -> listOf("/dictionary_small.txt")
            1 -> listOf("/dictionary_medium.txt")
            2 -> listOf("/dictionary00.txt")
            else -> listOf(
                "/dictionary00.txt",
                "/dictionary01.txt",
                "/dictionary02.txt",
                "/dictionary03.txt",
                "/dictionary04.txt",
                "/dictionary05.txt",
                "/dictionary06.txt",
                "/dictionary07.txt",
                "/dictionary08.txt",
                "/dictionary09.txt",
            )
        }

        val dictionaryList = dicUtils.getListDictionary(list)

        dictionaryList.forEach {
            tempList.add(it)
        }

        tempList.sortedBy { it.yomi.length }.groupBy { it.yomi }.forEach { entry ->
            yomiTree.insert(entry.key)
            println("insert to yomi tree: ${entry.key}")
            entry.value.forEach {
                if (it.yomi != it.tango && it.yomi.hiraToKata() != it.tango){
                    tangoTree.insert(it.tango)
                    println("insert to tango tree: ${it.tango}")
                }
            }
        }

        val yomiLOUDSTemp = ConverterWithTermId().convert(yomiTree.root)
        val tangoLOUDSTemp = Converter().convert(tangoTree.root)
        yomiLOUDSTemp.convertListToBitSet()
        tangoLOUDSTemp.convertListToBitSet()

        val objectOutputYomi = ObjectOutputStream(FileOutputStream("./src/main/resources/yomi.dat"))
        val objectOutputTango = ObjectOutputStream(FileOutputStream("./src/main/resources/tango.dat"))

        yomiLOUDSTemp.writeExternal(objectOutputYomi)
        tangoLOUDSTemp.writeExternal(objectOutputTango)

        val objectInputYomi = ObjectInputStream(FileInputStream("./src/main/resources/yomi.dat"))
        val objectInputTango = ObjectInputStream(FileInputStream("./src/main/resources/tango.dat"))

        val yomiLOUDS: LOUDSWithTermId = LOUDSWithTermId().readExternal(objectInputYomi)
        val tangoLOUDS: LOUDS = LOUDS().readExternal(objectInputTango)

        val tokenArrayTemp = TokenArray()

        val objectOutput = ObjectOutputStream(FileOutputStream("./src/main/resources/token.dat"))
        tokenArrayTemp.buildJunctionArray(
            tempList,
            tangoLOUDS,
            objectOutput,
            objectInputStreamForReadPOSTableWithIndex
        )

        val objectInput = ObjectInputStream(FileInputStream("./src/main/resources/token.dat"))
        val tokenArray = TokenArray()
        tokenArray.readExternal(objectInput)
        tokenArray.readPOSTable(objectInputStreamForReadPOSTable)
    }

    private fun buildConnectionIdSparseArray(){
        val lines = object {}::class.java.getResourceAsStream("/connection_single_column.txt")
            ?.bufferedReader()
            ?.readLines()

        val connectionIdBuilder = ConnectionIdBuilder()

        val objectOutput = ObjectOutputStream(FileOutputStream("./src/main/resources/connectionIds.dat"))
        lines?.let { l ->
            connectionIdBuilder.build(objectOutput,l.map { it.toShort() })
        }

        val objectInput = ObjectInputStream(FileInputStream("./src/main/resources/connectionIds.dat"))
        val a = ConnectionIdBuilder().read(objectInput)
        println("${a.size}")
    }

    fun loadBinaryFiles(
        objectInputStreamForReadPOSTable: ObjectInputStream
    ){
        var yomiTrie: LOUDSWithTermId
        var tangoTrie: LOUDS
        val graphBuilder = GraphBuilder()
        val connectionIds: List<Short>

        val objectInputYomi = ObjectInputStream(FileInputStream("src/test/resources/yomi.dat"))
        val objectInputTango = ObjectInputStream(FileInputStream("src/test/resources/tango.dat"))
        val objectInputTokenArray = ObjectInputStream(FileInputStream("src/test/resources/token.dat"))
        val objectInputConnectionId = ObjectInputStream(FileInputStream("src/test/resources/connectionIds.dat"))

        val tokenArray = TokenArray()

        yomiTrie = LOUDSWithTermId().readExternal(objectInputYomi)
        tangoTrie = LOUDS().readExternal(objectInputTango)
        tokenArray.readExternal(objectInputTokenArray)
        tokenArray.readPOSTable(objectInputStreamForReadPOSTable)
        connectionIds = ConnectionIdBuilder().read(objectInputConnectionId)

        val query = "とべないぶた"

        val graph = graphBuilder.constructGraph(
            query,
            yomiTrie,
            tangoTrie,
            tokenArray,
        )

        println("${graph.map { it.map { l -> l.map { u -> u.tango } } }}")

        val findPath = FindPath()

        println(findPath.backwardAStar(graph,query.length, connectionIds,1))

    }

    private fun getNBestPaths(
        kanaKanjiEngine: KanaKanjiEngine,
        input: String,
        n: Int
    ): List<String>{
        return kanaKanjiEngine.nBestPath(input,n)
    }

    private fun testBestPath(
        n: Int,
    ){
        val kanaKanjiEngine = KanaKanjiEngine()
        kanaKanjiEngine.buildEngine(this)

        val word1 = "とべないぶた"
        val word2 = "わたしのなまえはなかのです"
        val word3 = "ここではきものをぬぐ"

        kanaKanjiEngine.viterbiAlgorithm(word2)

        kanaKanjiEngine.nBestPath(word2,n)

        val result1BestPath = kanaKanjiEngine.viterbiAlgorithm(word1)
        val result2BestPath = kanaKanjiEngine.viterbiAlgorithm(word2)
        val result3BestPath = kanaKanjiEngine.viterbiAlgorithm(word3)

        val result1NBest = kanaKanjiEngine.nBestPath(word1,n)
        val result2NBest = kanaKanjiEngine.nBestPath(word2,n)
        val result3NBest = kanaKanjiEngine.nBestPath(word3,n)

        println("Viterbi $word1 =>=> $result1BestPath")
        println("Viterbi $word2 =>=> $result2BestPath")
        println("Viterbi $word3 =>=> $result3BestPath")

        println("nBestPath $word1 =>=> $result1NBest")
        println("nBestPath $word2 =>=> $result2NBest")
        println("nBestPath $word3 =>=> $result3NBest")

    }

}