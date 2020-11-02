package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleObserver

/**
 * This represents the contract by which the host app can interact with the
 * display unit of the mini app.
 * This contract complies to Android's [LifecycleObserver] contract, and when made to observe
 * the lifecycle, it automatically clears up the view state and any services registered with.
 */
interface MiniAppDisplay : LifecycleObserver {

    /**
     * Provides the view associated with the mini app to the caller for showing the mini app.
     * @param activityContext is used by the view for initializing the internal services.
     * Must be the context of activity to ensure that all standard html components work properly.
     * @return [View] as mini app's view with [LayoutParams] set to match
     * the parent's dimensions.
     * @throws MiniAppSdkException when a non-matching context is supplied
     */
    suspend fun getMiniAppView(activityContext: Context): View?

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
    fun destroyView()

    /**
     * Navigates one level back, in the call stack, if possible.
     */
    fun navigateBackward(): Boolean

    /**
     * Navigates one level forward from current position, in the call hierarchy, if possible.
     */
    fun navigateForward(): Boolean
}
