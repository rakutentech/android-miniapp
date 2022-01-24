package com.rakuten.tech.mobile.testapp.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppCoroutines {
    fun main(work: suspend (() -> Unit)) = CoroutineScope(Dispatchers.Main).launch {
        work()
    }

    fun io(work: suspend (() -> Unit)) = CoroutineScope(Dispatchers.IO).launch {
        work()
    }
}
