package com.kazumaproject.kana_kanji_converter.system

import android.content.Context
import com.kazumaproject.kana_kanji_converter.model.TokenEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.kazumaproject.trie4j.louds.TailLOUDSTrie
import java.io.DataInputStream
import java.io.ObjectInputStream

class SystemDictionaryLoader(private val context: Context) {
    suspend fun loadYomiDic():TailLOUDSTrie {
        val lt = TailLOUDSTrie()
        lt.readExternal(withContext(Dispatchers.IO) {
            ObjectInputStream(context.assets.open("system_trie/yomi.dic"))
        })
        return lt
    }

    fun loadTokenDef(): ArrayList<List<TokenEntry>> {
        val inputStream = DataInputStream(context.assets.open("system_trie/token.def"))
        val objectInputStream = ObjectInputStream(inputStream)
        return objectInputStream.readObject() as ArrayList<List<TokenEntry>>
    }

}