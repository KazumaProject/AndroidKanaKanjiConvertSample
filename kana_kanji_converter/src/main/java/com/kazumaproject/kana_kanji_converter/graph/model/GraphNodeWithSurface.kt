package com.kazumaproject.kana_kanji_converter.graph.model

data class GraphNodeWithSurface(
    val id: String,
    val nodeId: Int,
    val string: String,
    val cost: Int,
    val leftId: Int,
    val rightId: Int,
    val surface: String
)
