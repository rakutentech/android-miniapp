package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This represents the contract by which the host app can interact with the
 * display unit of the mini app.
 * This contract complies to Android's [LifecycleObserver] contract, and when made to observe
 * the lifecycle, it automatically clears up the view state and any services registered with.
 */
interface MiniAppDisplay : LifecycleObserver {
    val basePath: String
    val appId: String
    val miniAppMessageBridge: MiniAppMessageBridge

    /**
     * Provides the view associated with the mini app to the caller for showing the mini app.
     * Register the lifecycle as optional
     * @return [View] as mini app's view with [LayoutParams] set to match
     * the parent's dimensions.
     */
    suspend fun getMiniAppView(context: Context, hostLifecycle: Lifecycle? = null): View =
        withContext(Dispatchers.Main) {
            val view = RealMiniAppDisplay(context, basePath, appId, miniAppMessageBridge)
            hostLifecycle?.addObserver(view)
            view
        }

    /**
     * Upon invocation, destroys necessary view state and any services registered with.
     * When the consumer has finished consuming, it is advisable to release the resources.
     * Usual scenarios are when the app components are amidst their destroy cycle.
     * @see [link][https://developer.android.com/topic/libraries/architecture/lifecycle#lc]
     * on how to setup automatic clearing based on [LifecycleObserver] for usual scenarios.
     * To be called when resources are managed manually or where consumer app has more control
     * on the lifecycle of views e.g. removal of the view from the view system, yet
     * within the same state of parent's lifecycle.
     */
    fun destroyView() = Unit
}
