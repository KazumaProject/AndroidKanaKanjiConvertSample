package com.kazumaproject.kana_kanji_converter.graph

import android.util.Log
import com.kazumaproject.kana_kanji_converter.graph.model.GraphLine
import com.kazumaproject.kana_kanji_converter.graph.model.GraphNode
import com.kazumaproject.kana_kanji_converter.other.Constants
import com.kazumaproject.kana_kanji_converter.other.Constants.GRAPH_FILTER_AMOUNT
import com.kazumaproject.kana_kanji_converter.other.cartesianProduct
import com.kazumaproject.kana_kanji_converter.system.SystemDictionaryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jgrapht.GraphPath
import org.jgrapht.alg.shortestpath.EppsteinKShortestPath
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import com.kazumaproject.trie4j.louds.MapTailLOUDSTrie
import com.kazumaproject.trie4j.louds.TailLOUDSTrie
import java.io.StringWriter
import java.util.LinkedList
import java.util.Random
import java.util.UUID
import kotlin.math.min

class GraphBuilder {
//    suspend fun constructGraphAndGetResult(
//        queryText: String,
//        yomiTrie: TailLOUDSTrie,
//        systemDictionaryBuilder: SystemDictionaryBuilder,
//        connectionIds: ArrayList<Int>
//    ) : List<String> = CoroutineScope(Dispatchers.IO).async {
//
//        val startTime = System.currentTimeMillis()
//
//        val dictionaryDao = systemDictionaryBuilder.getSystemDictionaryDao()
//
//        val bos = GraphNode(UUID.randomUUID().toString(),"<BOS>",0,0,0)
//        val eos = GraphNode(UUID.randomUUID().toString(),"<EOS>",0,0,0)
//
//        val nLetterStartList = LinkedList(
//            mutableListOf(
//                mutableListOf(bos)
//            )
//        )
//        val nLetterEndList = LinkedList(
//            mutableListOf(
//                mutableListOf(eos)
//            )
//        )
//
//        val nLetterStartListFinal = LinkedList(
//            mutableListOf<List<GraphNode>>()
//        )
//        val nLetterEndListFinal = LinkedList(
//            mutableListOf<List<GraphNode>>()
//        )
//
//        val graph: SimpleDirectedWeightedGraph<GraphNode, GraphLine> = SimpleDirectedWeightedGraph(GraphLine::class.java)
//
//        launch {
//            for (i in queryText.indices + 1){
//                nLetterStartList.add(mutableListOf())
//                nLetterEndList.add(mutableListOf())
//            }
//            nLetterStartList[queryText.length + 1].add(GraphNode(UUID.randomUUID().toString(),"<EOS>",0,0,0))
//            nLetterEndList[queryText.length + 1].add(GraphNode(UUID.randomUUID().toString(),"<EOS>",0,0,0))
//
//            for (i in queryText.indices){
//                for (j in i + 1 .. min(queryText.length,32)){
//                    val subStr = queryText.substring(i,j)
//                    if (yomiTrie.contains(subStr)){
//                        val nodeId = yomiTrie.getNodeId(subStr)
//                        val dictionaryEntries = dictionaryDao.getDictionaryEntryListFromNodeId(nodeId)
//                        for (entry in dictionaryEntries){
//                            systemDictionaryBuilder.getSystemDictionaryDao().getDictionaryEntryListFromNodeId(entry.nodeId).forEach {
//                                println("Query: $subStr $it")
//                                val word = GraphNode(UUID.randomUUID().toString(),
//                                    dictionaryDao.getDictionaryEntryFromWordId(it.wordId).tango
//                                    ,it.c,
//                                    it.l,
//                                    it.r)
//                                nLetterStartList[i + 1].add(word)
//                                nLetterEndList[j].add(word)
//                            }
//                        }
//                    }
//                }
//            }
//
//        }.join()
//
//        launch {
//            for (i in 0 until nLetterStartList.size){
//                when(i){
//                    0 ->{
//                        nLetterStartListFinal.add(mutableListOf(bos))
//                    }
//                    nLetterStartList.lastIndex ->{
//                        nLetterStartListFinal.add(mutableListOf(eos))
//                    }
//                    else ->{
//                        val a = nLetterStartList[i].flatMap { g ->
//                            val nodeId = yomiTrie.getNodeId(g.string)
//                            dictionaryDao.getDictionaryEntryListFromNodeId(nodeId).flatMap { t ->
//                                t.c.map {
//                                    GraphNode(UUID.randomUUID().toString(),
//                                        tangoTrie.LOUDSNode(it.s).letters.joinToString()
//                                        ,it.c,
//                                        it.l,
//                                        it.r)
//                                }
//                            }
//                        }
//                        nLetterStartListFinal.add(a)
//                    }
//                }
//            }
//            for (i in 0 until nLetterEndList.size){
//                when(i){
//                    0 ->{
//                        nLetterEndListFinal.add(mutableListOf(bos))
//                    }
//                    nLetterEndList.lastIndex ->{
//                        nLetterEndListFinal.add(mutableListOf(eos))
//                    }
//                    else ->{
//                        val a = nLetterEndList[i].flatMap { g ->
//                            val nodeId = yomiTrie.getNodeId(g.string)
//                            dictionaryDao.getDictionaryEntryListFromNodeId(nodeId).flatMap { t ->
//                                t.c.map {
//                                    GraphNode(UUID.randomUUID().toString(),
//                                        tangoTrie.LOUDSNode(it.s).letters.joinToString()
//                                        ,it.c,
//                                        it.l,
//                                        it.r)
//                                }
//                            }
//                        }
//                        nLetterEndListFinal.add(a)
//                    }
//                }
//            }
//        }.join()
//
//        launch {
//            graph.apply {
//
//                addVertex(bos)
//                addVertex(eos)
//
//                when(queryText.length){
//                    0 ->{}
//                    1 ->{
//                        for (i in 0 until nLetterStartListFinal.size){
//                            when(i){
//                                0 ->{}
//                                else ->{
//                                    val previous = nLetterEndListFinal[i - 1].sortedBy { it.cost }.distinctBy { it.string }
//
//                                    val cur = nLetterStartListFinal[i].sortedBy { it.cost }.distinctBy { it.string }
//
//                                    val combinations = previous.cartesianProduct(cur)
//
//                                    for (nodePair in combinations){
//                                        addVertex(nodePair.first)
//                                        Log.d("added first vertex","${nodePair.first.string} ${nodePair.first.cost}")
//                                        addVertex(nodePair.second)
//                                        Log.d("added second vertex","${nodePair.second.string} ${nodePair.second.cost}")
//                                        val cost = when{
//                                            nodePair.first.leftId == 374 ->{
//                                                3000
//                                            }
//                                            nodePair.second.rightId == 143 ->{
//                                                3000
//                                            }
//                                            nodePair.first.leftId == 2586 && nodePair.second.rightId == 374 ||  nodePair.first.leftId == 283 && nodePair.second.rightId == 2586  -> -1500
//                                            nodePair.first.cost >= 5000 -> 100000
//                                            nodePair.second.cost >= 5000 -> 100000
//                                            else ->{
//                                                nodePair.first.cost + nodePair.second.cost
//                                            }
//                                        }
//                                        val totalCost = if (cost + nodePair.second.cost < 0) 0 else cost + nodePair.second.cost
//                                        val edge = GraphLine(UUID.randomUUID().toString(),totalCost)
//                                        addEdge(nodePair.first,nodePair.second,edge)
//                                        setEdgeWeight(edge,totalCost.toDouble())
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    else ->{
//                        for (i in 0 until nLetterStartListFinal.size){
//                            when(i){
//                                0 ->{}
//                                else ->{
//                                    var previous = nLetterEndListFinal[i - 1].sortedBy { it.cost }.distinctBy { it.string }
//                                    if (previous.size >= GRAPH_FILTER_AMOUNT) previous = previous.slice(
//                                        0 until GRAPH_FILTER_AMOUNT
//                                    )
//                                    var cur = nLetterStartListFinal[i] .sortedBy { it.cost }.distinctBy { it.string }
//                                    if (cur.size >= GRAPH_FILTER_AMOUNT) cur = cur.slice(0 until GRAPH_FILTER_AMOUNT)
//
//                                    val combinations = previous.cartesianProduct(cur)
//
//                                    for (nodePair in combinations){
//                                        addVertex(nodePair.first)
//                                        Log.d("added first vertex","${nodePair.first.string} ${nodePair.first.cost} ${nodePair.first.leftId}")
//                                        addVertex(nodePair.second)
//                                        Log.d("added second vertex","${nodePair.second.string} ${nodePair.second.cost} ${nodePair.second.rightId}")
//                                        val cost = when{
//                                            nodePair.first.leftId == 374 ->{
//                                                3000
//                                            }
//                                            nodePair.second.rightId == 143 ->{
//                                                3000
//                                            }
//                                            nodePair.first.leftId == 2586 && nodePair.second.rightId == 374 ||  nodePair.first.leftId == 283 && nodePair.second.rightId == 2586  -> -1500
//                                            nodePair.first.cost >= 5000 -> 100000
//                                            nodePair.second.cost >= 5000 -> 100000
//                                            nodePair.first.leftId == 381 || nodePair.second.rightId == 381 -> 8000
//                                            nodePair.first.leftId == 332 || nodePair.second.rightId == 332 -> 1000
//                                            nodePair.first.leftId == 392 || nodePair.second.rightId == 392 -> 0
//                                            else ->{
//                                                val cId = nodePair.first.leftId * Constants.TOTAL_ID_SIZE + nodePair.second.rightId
//                                                connectionIds[cId]
//                                            }
//                                        }
//                                        val totalCost = if (cost + nodePair.second.cost < 0) 0 else cost + nodePair.second.cost
//                                        val edge = GraphLine(UUID.randomUUID().toString(),totalCost)
//                                        addEdge(nodePair.first,nodePair.second,edge)
//                                        setEdgeWeight(edge,totalCost.toDouble())
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
//        }.join()
//
//        val dijkstraShortestPath = EppsteinKShortestPath(graph)
//        val a = dijkstraShortestPath.getPaths(bos,eos,100)
//
//        val endTime = System.currentTimeMillis()
//        val elapsedTime = (endTime - startTime) / 1000
//
//        val exporter = DOTExporter<GraphNode,GraphLine>()
//        exporter.setVertexAttributeProvider {
//            val map = LinkedHashMap<String, Attribute>()
//            map["string&cost"] = DefaultAttribute.createAttribute("${it.string} ${it.cost} ${it.leftId} ${it.rightId}")
//            return@setVertexAttributeProvider map
//        }
//        val writer = StringWriter()
//        exporter.exportGraph(graph,writer)
//
//        //Log.d("graph writer","$writer")
//
//        println("Execution time: $elapsedTime sec")
//
//        return@async getListRemovedBOSandEOS(a)
//            .drop(1)
//            .dropLast(1)
//            .split(",")
//    }.await()

    private suspend fun getListRemovedBOSandEOS(
        path: List<GraphPath<GraphNode, GraphLine>>
    ): String = CoroutineScope(Dispatchers.IO).async {
        return@async path.map {
            val list = it.vertexList
            list.subList(1,list.size).subList(0,list.size - 2).joinToString("") { m ->
                m.string
            } //+ " ${it.weight}"
        }.toString()
    }.await()

}
