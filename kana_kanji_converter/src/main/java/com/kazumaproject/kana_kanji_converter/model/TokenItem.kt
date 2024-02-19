package com.kazumaproject.kana_kanji_converter.model

data class TokenItem(
    val surface: String,
    val leftId: Short,
    val rightId: Short,
    val cost: Short,
    val idAfterConversion: Int
)
