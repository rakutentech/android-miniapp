package com.rakuten.tech.mobile.miniapp

import android.view.View

/**
 * This represents the contract by which the host app can interact with the
 * display unit of the mini app.
 */
interface MiniAppDisplay { // This contract keeps the display of a mini app implementation agnostic

    /**
     * Provides the view associated with the mini app to the caller for showing the mini app.
     * @return [View] as mini app's view with [LayoutParams] set to match
     * the parent's dimensions.
     */
    fun getMiniAppView(): View
}
