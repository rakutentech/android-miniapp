package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class Displayer(val context: Context) {

    suspend fun createMiniAppDisplay(basePath: String): MiniAppDisplay =
        withContext(Dispatchers.Main) {
            context?.let {
                RealMiniAppDisplay(context, basePath)
            }
        }
}
