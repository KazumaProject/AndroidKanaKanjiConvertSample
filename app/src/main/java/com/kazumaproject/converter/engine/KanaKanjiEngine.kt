package com.kazumaproject.converter.engine

import android.content.Context
import com.kazumaproject.Louds.LOUDS
import com.kazumaproject.Louds.with_term_id.LOUDSWithTermId
import com.kazumaproject.connection_id.ConnectionIdBuilder
import com.kazumaproject.dictionary.TokenArray
import com.kazumaproject.converter.graph.GraphBuilder
import com.kazumaproject.viterbi.FindPath
import java.io.FileInputStream
import java.io.ObjectInputStream

class KanaKanjiEngine {

    private lateinit var yomiTrie: LOUDSWithTermId
    private lateinit var tangoTrie: LOUDS
    private lateinit var connectionIds: List<Short>
    private lateinit var tokenArray: TokenArray

    fun buildEngine(context: Context) {
        val objectInputYomi = ObjectInputStream(context.assets.open("yomi.dat"))
        val objectInputTango = ObjectInputStream(context.assets.open("tango.dat"))
        val objectInputTokenArray = ObjectInputStream(context.assets.open("token.dat"))
        val objectInputConnectionId = ObjectInputStream(context.assets.open("connectionIds.dat"))
        val objectInputReadPOSTable = ObjectInputStream(context.assets.open("pos_table.dat"))

        yomiTrie = LOUDSWithTermId().readExternal(objectInputYomi)
        tangoTrie = LOUDS().readExternal(objectInputTango)

        tokenArray = TokenArray()
        tokenArray.readExternal(objectInputTokenArray)
        tokenArray.readPOSTable(objectInputReadPOSTable)
//
        connectionIds = ConnectionIdBuilder().read(objectInputConnectionId)
    }

    fun buildEngineForTest(
        objectInputStreamForReadPOSTable: ObjectInputStream
    ){
        val objectInputYomi = ObjectInputStream(FileInputStream("src/test/resources/yomi.dat"))
        val objectInputTango = ObjectInputStream(FileInputStream("src/test/resources/tango.dat"))
        val objectInputTokenArray = ObjectInputStream(FileInputStream("src/test/resources/token.dat"))
        val objectInputConnectionId = ObjectInputStream(FileInputStream("src/test/resources/connectionIds.dat"))
        yomiTrie = LOUDSWithTermId().readExternal(objectInputYomi)
        tangoTrie = LOUDS().readExternal(objectInputTango)
        tokenArray = TokenArray()
        tokenArray.readExternal(objectInputTokenArray)
        tokenArray.readPOSTable(objectInputStreamForReadPOSTable)
        connectionIds = ConnectionIdBuilder().read(objectInputConnectionId)
    }

    fun nBestPath(
        input: String,
        n: Int
    ): List<String> {
        val findPath = FindPath()
        val graphBuilder = GraphBuilder()
        val graph = graphBuilder.constructGraph(
            input,
            yomiTrie,
            tangoTrie,
            tokenArray,
        )
        return findPath.backwardAStar(graph, input.length, connectionIds, n)
    }

    fun viterbiAlgorithm(
        input: String
    ): String {
        val findPath = FindPath()
        val graphBuilder = GraphBuilder()
        val graph = graphBuilder.constructGraph(
            input,
            yomiTrie,
            tangoTrie,
            tokenArray,
        )
        return findPath.viterbi(graph, input.length, connectionIds)
    }

}