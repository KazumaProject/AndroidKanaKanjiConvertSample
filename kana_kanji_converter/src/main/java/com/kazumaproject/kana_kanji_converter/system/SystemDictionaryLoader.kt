package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.kazumaproject.trie4j.louds.TailLOUDSTrie
import java.io.ObjectInputStream

class SystemDictionaryLoader(private val context: Context) {
    suspend fun loadYomiDic():TailLOUDSTrie {
        val lt = TailLOUDSTrie()
        lt.readExternal(withContext(Dispatchers.IO) {
            ObjectInputStream(context.assets.open("system_trie/yomi.dic"))
        })
        return lt
    }

}