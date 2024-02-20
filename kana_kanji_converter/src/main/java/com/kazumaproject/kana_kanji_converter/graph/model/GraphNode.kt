package com.kazumaproject.kana_kanji_converter.graph.model

data class GraphNode(
    val id: String,
    val nodeId: Int,
    val string: String,
    val cost: Int,
    val leftId: Int,
    val rightId: Int,
)
