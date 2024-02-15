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
import org.trie4j.louds.TailLOUDSTrie
import java.io.StringWriter
import java.util.LinkedList
import java.util.Random
import java.util.UUID
import kotlin.math.min

class GraphBuilder {
    suspend fun constructGraphAndGetResult(
        queryText: String,
        yomiTrie: TailLOUDSTrie,
        systemDictionaryBuilder: SystemDictionaryBuilder
    ) : List<String> = CoroutineScope(Dispatchers.IO).async {

        val startTime = System.currentTimeMillis()

        val dictionaryDao = systemDictionaryBuilder.getSystemDictionaryDao()
        val connectionIDDao = systemDictionaryBuilder.getConnectionIdDao()

        val bos = GraphNode(UUID.randomUUID().toString(),"<BOS>",0,0,0)
        val eos = GraphNode(UUID.randomUUID().toString(),"<EOS>",0,0,0)


        val nLetterStartList = LinkedList(
            mutableListOf(
                mutableListOf(bos)
            )
        )
        val nLetterEndList = LinkedList(
            mutableListOf(
                mutableListOf(eos)
            )
        )

        val nLetterStartListFinal = LinkedList(
            mutableListOf<List<GraphNode>>()
        )
        val nLetterEndListFinal = LinkedList(
            mutableListOf<List<GraphNode>>()
        )

        val graph: SimpleDirectedWeightedGraph<GraphNode, GraphLine> = SimpleDirectedWeightedGraph(GraphLine::class.java)

        launch {
            for (i in queryText.indices + 1){
                nLetterStartList.add(mutableListOf())
                nLetterEndList.add(mutableListOf())
            }
            nLetterStartList[queryText.length + 1].add(GraphNode(UUID.randomUUID().toString(),"<EOS>",0,0,0))
            nLetterEndList[queryText.length + 1].add(GraphNode(UUID.randomUUID().toString(),"<EOS>",0,0,0))

            for (i in queryText.indices){
                for (j in i + 1 .. min(queryText.length,32)){
                    val subStr = queryText.substring(i,j)
                    if (yomiTrie.contains(subStr)){
                        val nodeId = yomiTrie.getNodeId(subStr)
                        val dictionaryEntries = dictionaryDao.getDictionaryEntryListFromNodeId(nodeId)
                        for (entry in dictionaryEntries){
                            for (d in entry.features){
                                val converted = d.c
                                val cost = d.c
                                val leftId = d.l
                                val rightId = d.r
                                println("Query: $subStr $converted $cost $i $j $leftId $rightId")
                                val word = GraphNode(UUID.randomUUID().toString(),subStr,cost, leftId,rightId)
                                nLetterStartList[i + 1].add(word)
                                nLetterEndList[j].add(word)
                            }
                        }
                    }
                }
            }

        }.join()

        launch {
            for (i in 0 until nLetterStartList.size){
                when(i){
                    0 ->{
                        nLetterStartListFinal.add(mutableListOf(bos))
                    }
                    nLetterStartList.lastIndex ->{
                        nLetterStartListFinal.add(mutableListOf(eos))
                    }
                    else ->{
                        val a = nLetterStartList[i].flatMap { g ->
                            val nodeId = yomiTrie.getNodeId(g.string)
                            dictionaryDao.getDictionaryEntryListFromNodeId(nodeId).flatMap { t ->
                                t.features.map { d ->
                                    GraphNode(g.id,d.t, d.c,d.l,d.r)
                                }
                            }
                        }
                        nLetterStartListFinal.add(a)
                    }
                }
            }
            for (i in 0 until nLetterEndList.size){
                when(i){
                    0 ->{
                        nLetterEndListFinal.add(mutableListOf(bos))
                    }
                    nLetterEndList.lastIndex ->{
                        nLetterEndListFinal.add(mutableListOf(eos))
                    }
                    else ->{
                        val a = nLetterEndList[i].flatMap { g ->
                            val nodeId = yomiTrie.getNodeId(g.string)
                            dictionaryDao.getDictionaryEntryListFromNodeId(nodeId).flatMap { t ->
                                t.features.map { d ->
                                    GraphNode(g.id,d.t, d.c,d.l,d.l)
                                }
                            }
                        }
                        nLetterEndListFinal.add(a)
                    }
                }
            }
        }.join()

        launch {
            graph.apply {

                addVertex(bos)
                addVertex(eos)

                for (i in 0 until nLetterStartListFinal.size){
                    when(i){
                        0 ->{}
                        else ->{
                            var previous = nLetterEndListFinal[i - 1].sortedBy { it.cost }.distinctBy { it.string }
                            if (previous.size >= GRAPH_FILTER_AMOUNT) previous = previous.slice(
                                0 until GRAPH_FILTER_AMOUNT
                            )
                            var cur = nLetterStartListFinal[i].sortedBy { it.cost }.distinctBy { it.string }
                            if (cur.size >= GRAPH_FILTER_AMOUNT) cur = cur.slice(0 until GRAPH_FILTER_AMOUNT)

                            val combinations = previous.cartesianProduct(cur)
                            for (nodePair in combinations){
                                addVertex(nodePair.first)
                                Log.d("added first vertex","${nodePair.first.string} ${nodePair.first.cost}")
                                addVertex(nodePair.second)
                                Log.d("added second vertex","${nodePair.second.string} ${nodePair.second.cost}")
                                val cost = when{
                                    nodePair.first.leftId == 374 ->{
                                        3000
                                    }
                                    nodePair.second.rightId == 143 ->{
                                        3000
                                    }
                                    nodePair.first.leftId == 2586 && nodePair.second.rightId == 374 ||  nodePair.first.leftId == 283 && nodePair.second.rightId == 2586  -> -1500
                                    nodePair.first.cost >= 5000 -> 100000
                                    nodePair.second.cost >= 5000 -> 100000
                                    else ->{
                                        val cId = nodePair.first.leftId * Constants.TOTAL_ID_SIZE + nodePair.second.rightId
                                        connectionIDDao.getCostFromConnectionID(cId).cost
                                    }
                                }
                                val totalCost = if (cost + nodePair.second.cost < 0) 0 else cost + nodePair.second.cost
                                val edge = GraphLine(UUID.randomUUID().toString(),totalCost)
                                addEdge(nodePair.first,nodePair.second,edge)
                                setEdgeWeight(edge,totalCost.toDouble())
                            }

//                            for (c in cur){
//                                addVertex(c)
//                                for (p in previous){
//                                    val cost = when{
//                                        p.leftId == 374 ->{
//                                            3000
//                                        }
//                                        c.rightId == 143 ->{
//                                            3000
//                                        }
//                                        p.leftId == 2586 && c.rightId == 374 ||  p.leftId == 283 && c.rightId == 2586  -> -1500
//                                        else ->{
//                                            connectionIDDao.getCostFromConnectionID(
//                                                p.leftId * Constants.TOTAL_ID_SIZE + c.rightId).cost
//                                        }
//                                    }
//                                    val totalCost = if (cost + c.cost < 0) 0 else cost + c.cost
//
//                                    Log.d("insert graph node","${p.string} ${p.cost} ${p.leftId} -- $totalCost --> ${c.string} ${c.cost} ${c.rightId}")
//
//                                    addVertex(p)
//                                    val edge = GraphLine(UUID.randomUUID().toString(),totalCost)
//                                    if (p != c){
//                                        addEdge(p,c,edge)
//                                        setEdgeWeight(edge,edge.cost.toDouble())
//                                    }
//                                }
//
//                            }
                        }
                    }
                }

//                for (node in nodeMap){
//                    addVertex(node.value.first)
//                    addVertex(node.value.second)
//
//                    println("previous: ${node.value.first.string}\ncurrent: ${node.value.second.string}")
//
//                    val cost = when{
//                        node.value.first.leftId == 374 ->{
//                            3000
//                        }
//                        node.value.second.rightId == 143 ->{
//                            3000
//                        }
//                        node.value.first.leftId == 2586 && node.value.second.rightId == 374 ||  node.value.first.leftId == 283 && node.value.second.rightId == 2586  -> -1500
//                        else ->{
//                            connectionIDDao.getCostFromConnectionID(
//                                node.value.first.leftId * Constants.TOTAL_ID_SIZE + node.value.second.rightId).cost
//                        }
//                    }
//                    val totalCost = if (cost + node.value.second.cost < 0) 0 else cost + node.value.second.cost
//                    val edge = GraphLine(
//                        Random().nextInt(),
//                        totalCost
//                    )
//                    addEdge(node.value.first, node.value.second, edge)
//                    setEdgeWeight(edge, totalCost.toDouble())
//                }

            }
        }.join()

        val dijkstraShortestPath = EppsteinKShortestPath(graph)
        val a = dijkstraShortestPath.getPaths(bos,eos,100)

        val endTime = System.currentTimeMillis()
        val elapsedTime = (endTime - startTime) / 1000

        val exporter = DOTExporter<GraphNode,GraphLine>()
        exporter.setVertexAttributeProvider {
            val map = LinkedHashMap<String, Attribute>()
            map["string&cost"] = DefaultAttribute.createAttribute("${it.string} ${it.cost} ${it.leftId} ${it.rightId}")
            return@setVertexAttributeProvider map
        }
        val writer = StringWriter()
        exporter.exportGraph(graph,writer)

        //Log.d("graph writer","$writer")

        println("Execution time: $elapsedTime sec")

        return@async getListRemovedBOSandEOS(a)
            .drop(1)
            .dropLast(1)
            .split(",")
    }.await()

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
