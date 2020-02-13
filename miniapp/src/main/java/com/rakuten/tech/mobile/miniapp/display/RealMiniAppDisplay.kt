package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.view.View
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RealMiniAppDisplay(
    val basePath: String
) : MiniAppDisplay {

    override suspend fun obtainView(context: Context): View = withContext(Dispatchers.Main) {
        MiniAppWindow(context, basePath)
    }
}
