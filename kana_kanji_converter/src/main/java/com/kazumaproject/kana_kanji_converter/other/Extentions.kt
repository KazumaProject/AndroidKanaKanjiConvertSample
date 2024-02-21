package com.kazumaproject.kana_kanji_converter.other

fun <T, S> Collection<T>.cartesianProduct(other: Iterable<S>): List<Pair<T, S>> {
    return cartesianProduct(other) { first, second -> first to second }
}

fun <T, S, V> Collection<T>.cartesianProduct(other: Iterable<S>, transformer: (first: T, second: S) -> V): List<V> {
    return this.flatMap { first -> other.map { second -> transformer.invoke(first, second) } }
}

fun String.hiraToKata() =
    this.map {
        if (it.code in 0x3041..0x3093) {
            it + 0x60
        } else {
            it
        }
    }.joinToString("")