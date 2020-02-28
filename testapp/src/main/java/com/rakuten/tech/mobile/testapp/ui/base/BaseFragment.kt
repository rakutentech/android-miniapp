package com.rakuten.tech.mobile.testapp.ui.base

import androidx.fragment.app.Fragment
import com.rakuten.tech.mobile.testapp.helper.SingleExecution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

open class BaseFragment : Fragment(), CoroutineScope {
    private val job: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    val singleExecution= SingleExecution()

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
