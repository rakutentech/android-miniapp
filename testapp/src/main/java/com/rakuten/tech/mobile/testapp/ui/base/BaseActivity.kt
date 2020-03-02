package com.rakuten.tech.mobile.testapp.ui.base

import androidx.appcompat.app.AppCompatActivity
import com.rakuten.tech.mobile.testapp.helper.RaceExecutor
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity: AppCompatActivity(), CoroutineScope {
    private val job: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    val raceExecutor = RaceExecutor()

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
