package com.rakuten.tech.mobile.miniapp

import android.app.Activity
import java.io.File

class TestActivity : Activity() {
    private val filesDir = ""
    override fun getFilesDir(): File = File(filesDir)
}
