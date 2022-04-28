package com.rakuten.tech.mobile.miniapp

import android.app.Activity
import java.io.File

class TestActivity : Activity()
class TestFilesDirActivity : Activity() {
    override fun getFilesDir(): File {
        return File("")
    }
}
