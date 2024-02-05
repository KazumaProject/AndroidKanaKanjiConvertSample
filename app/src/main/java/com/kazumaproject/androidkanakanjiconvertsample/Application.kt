package com.kazumaproject.androidkanakanjiconvertsample

import java.io.File

class Application: android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
    }
}