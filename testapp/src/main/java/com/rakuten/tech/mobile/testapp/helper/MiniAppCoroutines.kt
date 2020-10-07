package com.rakuten.tech.mobile.testapp.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class MiniAppCoroutines(
    val mainDispatcher: CoroutineContext = Dispatchers.Main
) {
    fun buildMainScope(job: Job): CoroutineScope = CoroutineScope(mainDispatcher + job)
}
