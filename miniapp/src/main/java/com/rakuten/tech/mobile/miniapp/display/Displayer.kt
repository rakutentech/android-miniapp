package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppMessageInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class Displayer(private val context: Context) {

    suspend fun createMiniAppDisplay(
        basePath: String,
        appId: String,
        miniAppMessageInterface: MiniAppMessageInterface
    ): MiniAppDisplay =
        withContext(Dispatchers.Main) {
            context?.let {
                RealMiniAppDisplay(context, basePath, appId, miniAppMessageInterface)
            }
        }
}
