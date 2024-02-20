package com.kazumaproject.kana_kanji_converter.model

import java.io.Serializable

data class TokenEntry(
    val leftId: Short,
    val rightId: Short,
    val cost: Short,
    val tangoId: Int
): Serializable
