package com.rakuten.tech.mobile.testapp.ui.base

import androidx.appcompat.app.AppCompatActivity
import com.rakuten.tech.mobile.testapp.analytics.rat_wrapper.RATActivity
import com.rakuten.tech.mobile.testapp.helper.RaceExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity : RATActivity(), CoroutineScope {
    private val job: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    val raceExecutor = RaceExecutor()

    protected fun showBackIcon() {
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
