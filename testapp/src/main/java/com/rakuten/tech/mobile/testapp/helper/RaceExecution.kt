package com.rakuten.tech.mobile.testapp.helper

import android.os.SystemClock

/**
 * The simple controller which ensures only one method executed at a time.
 */
class RaceExecution {
    private var interval:Int = 800
    private var lastTimeClicked:Long = 0

    fun run(func: () -> Unit) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked > interval) {
            lastTimeClicked = SystemClock.elapsedRealtime()
            func()
        }
    }
}
