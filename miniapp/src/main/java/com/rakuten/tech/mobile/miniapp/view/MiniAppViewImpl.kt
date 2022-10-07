package com.rakuten.tech.mobile.miniapp.view

import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class MiniAppViewImpl(
    internal var miniAppParameters: MiniAppParameters,
    initMiniAppViewHandler: () -> MiniAppViewHandler,
) : MiniAppView() {

    private val miniAppViewHandler: MiniAppViewHandler by lazy { initMiniAppViewHandler() }
    internal var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun load(queryParams: String, onComplete: (MiniAppDisplay) -> Unit) {
        scope.launch {
            when (miniAppParameters) {
                is MiniAppParameters.DefaultParams -> {
                    if (queryParams != "") (miniAppParameters as MiniAppParameters.DefaultParams).config.queryParams =
                        queryParams
                    onComplete(
                        miniAppViewHandler.createMiniAppView(
                            (miniAppParameters as MiniAppParameters.DefaultParams).miniAppId,
                            (miniAppParameters as MiniAppParameters.DefaultParams).config,
                            (miniAppParameters as MiniAppParameters.DefaultParams).fromCache
                        )
                    )
                }
                is MiniAppParameters.InfoParams -> scope.launch {
                    if (queryParams != "") (miniAppParameters as MiniAppParameters.InfoParams).config.queryParams =
                        queryParams
                    onComplete(
                        miniAppViewHandler.createMiniAppView(
                            (miniAppParameters as MiniAppParameters.InfoParams).miniAppInfo,
                            (miniAppParameters as MiniAppParameters.InfoParams).config,
                            (miniAppParameters as MiniAppParameters.InfoParams).fromCache
                        )
                    )
                }
                is MiniAppParameters.UrlParams -> scope.launch {
                    if (queryParams != "") (miniAppParameters as MiniAppParameters.UrlParams).config.queryParams =
                        queryParams
                    onComplete(
                        miniAppViewHandler.createMiniAppViewWithUrl(
                            (miniAppParameters as MiniAppParameters.UrlParams).miniAppUrl,
                            (miniAppParameters as MiniAppParameters.UrlParams).config
                        )
                    )
                }
            }
        }
    }
}
