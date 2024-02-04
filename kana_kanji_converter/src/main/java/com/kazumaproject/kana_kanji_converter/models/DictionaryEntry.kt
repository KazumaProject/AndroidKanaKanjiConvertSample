package com.kazumaproject.kana_kanji_converter.models

data class DictionaryEntry(
    val surface: String,
    val leftID: Int,
    val rightID: Int,
    val wordCost: Int,
    val afterConversion: String
)
