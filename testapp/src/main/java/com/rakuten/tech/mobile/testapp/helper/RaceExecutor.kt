package com.rakuten.tech.mobile.testapp.helper

import android.os.SystemClock

/**
 * A simple controller that ensures only one method is executed at a time where there is a race between two.
 */
class RaceExecutor {
    private var interval:Int = 800
    private var lastTimeClicked:Long = 0

    fun run(func: () -> Unit) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked > interval) {
            lastTimeClicked = SystemClock.elapsedRealtime()
            func()
        }
    }
}
